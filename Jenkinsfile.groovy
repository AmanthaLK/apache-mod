pipeline {
    agent any

    stages {
        stage('Check Local File') {
            steps {
                script {
                    sh '''
                        echo "+++++++++Current directory:+++++++++"
                        pwd
                        echo "+++++++++Listing files:+++++++++"
                        ls -l
                        echo "+++++++++Displaying index.html content:+++++++++"
                        cat index.html
                    '''
                }
            }
        }

        stage('Copy to Remote Server') {
            steps {
                script {
                    sshagent(['aws-personal']) {
                        sh '''
                            echo "+++++++++Copying index.html to remote server+++++++++"
                            scp -o StrictHostKeyChecking=no -i /var/lib/jenkins/workspace/apache-modification@tmp/private_key_2022475152950786097.key index.html ubuntu@172.31.82.42:/tmp/index.html
                            echo "+++++++++Taking root access and moving file+++++++++"
                            ssh -o StrictHostKeyChecking=no -i /var/lib/jenkins/workspace/apache-modification@tmp/private_key_2022475152950786097.key ubuntu@172.31.82.42 \
                                'sudo -i bash -c "mv /tmp/index.html /var/www/html/index.html && systemctl reload apache2.service"'
                        '''
                    }
                }
            }
        }

        stage('List S3 Buckets') {
            steps {
                script {
                    sh '''
                        echo "+++++++++Listing all S3 buckets+++++++++"
                        aws s3 ls
                    '''
                }
            }
        }

        stage('Upload to S3 Bucket') {
            steps {
                script {
                    def dateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                    sh """
                        echo "+++++++++Creating folder in S3 bucket and uploading files+++++++++"
                        aws s3api put-object --bucket apache-backups-amantha --key ${dateTime}/
                        aws s3 cp index.html s3://apache-backups-amantha/${dateTime}/index.html
                        aws s3 cp Jenkinsfile.groovy s3://apache-backups-amantha/${dateTime}/Jenkinsfile.groovy
                    """
                }
            }
        }

        stage('Run SonarQube Scanner') {
            environment {
                SONARQUBE_SERVER = 'http://3.80.137.180:9000/' // Replace with your SonarQube server URL
                SONARQUBE_TOKEN = credentials('sq-id') // Ensure you have this credential ID in Jenkins
            }
            steps {
                script {
                    sh '''
                        echo "+++++++++Running SonarQube Scanner+++++++++"
                        sonar-scanner \
                            -Dsonar.projectKey=sq-apache \
                            -Dsonar.sources=. \
                            -Dsonar.host.url=${SONARQUBE_SERVER} \
                            -Dsonar.login=${SONARQUBE_TOKEN}
                    '''
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
    }
}
