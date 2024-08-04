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

        stage('SonarQube Scan') {
            steps {
                script {
                    def sonarScannerHome = tool 'SonarQube Scanner 6.1.0.4477'  // Use the exact tool name
                    withCredentials([string(credentialsId: 'sq-id', variable: 'SONAR_TOKEN')]) {
                        sh """
                            echo "+++++++++Running SonarQube scan+++++++++"
                            ${sonarScannerHome}/bin/sonar-scanner \
                                -Dsonar.projectKey=sq-jenkins-test \
                                -Dsonar.sources=. \
                                -Dsonar.host.url=http://3.80.137.180:9000/ \
                                -Dsonar.login=${SONAR_TOKEN}
                        """
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

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
    }
}
