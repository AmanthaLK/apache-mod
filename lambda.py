import json
import boto3
import datetime
from io import StringIO
import base64

def lambda_handler(event, context):
    ec2 = boto3.client('ec2')
    s3 = boto3.client('s3')
    ses = boto3.client('ses')
    
    report = []

    paginator = ec2.get_paginator('describe_instances')
    for page in paginator.paginate():
        for reservation in page['Reservations']:
            for instance in reservation['Instances']:
                instance_id = instance['InstanceId']
                instance_name = 'N/A'
                
                tags = instance.get('Tags', [])
                for tag in tags:
                    if tag['Key'] == 'Name':
                        instance_name = tag['Value']
                        break
                
                security_groups = instance.get('SecurityGroups', [])
                for sg in security_groups:
                    sg_id = sg['GroupId']
                    
                    sg_response = ec2.describe_security_groups(GroupIds=[sg_id])
                    for sg in sg_response['SecurityGroups']:
                        for rule in sg['IpPermissions']:
                            port_range = f"{rule.get('FromPort', 'N/A')}-{rule.get('ToPort', 'N/A')}"
                            for ip_range in rule.get('IpRanges', []):
                                cidr_ip = ip_range.get('CidrIp', 'N/A')
                                report.append(f"{instance_id}, {instance_name}, {port_range}, {cidr_ip}")
    
    report_txt = "Instance ID, Instance Name, Port/Port range, Source\n" + "\n".join(report)
    
    current_datetime = datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')
    filename = f"lambda-report_{current_datetime}.txt"
    
    s3.put_object(
        Bucket='apache-backups-amantha',
        Key=filename,
        Body=report_txt,
        ContentType='text/plain'
    )
    
    s3_object_url = f"https://s3.amazonaws.com/apache-backups-amantha/{filename}"
    
    encoded_report = base64.b64encode(report_txt.encode('utf-8')).decode('utf-8')

    email_subject = 'EC2 Security Group Report'
    email_body = 'Please find the attached EC2 security group report.'
    
    raw_email = f"""From: "AWS Lambda" <amanthamihiranga@gmail.com>
To: "Amantha Amarasena" <amantha.amarasena@sysco.com>
Subject: {email_subject}
MIME-Version: 1.0
Content-Type: multipart/mixed; boundary="NextPart"

--NextPart
Content-Type: text/plain

{email_body}

--NextPart
Content-Type: text/plain; name="{filename}"
Content-Disposition: attachment; filename="{filename}"
Content-Transfer-Encoding: base64

{encoded_report}
--NextPart--
"""

    response = ses.send_raw_email(
        RawMessage={
            'Data': raw_email
        }
    )
    
    print(f"Email sent with message ID: {response['MessageId']}")

    return {
        'statusCode': 200,
        'body': json.dumps({'s3_url': s3_object_url}, indent=4)
    }
