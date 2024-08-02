pipeline {
    agent any

    stages {
        stage('Check Local File') {
            steps {
                script {
                    // Print the current directory, list files, and display the content of index.html
                    sh '''
                        echo "Current directory:"
                        pwd
                        echo "Listing files:"
                        ls -l
                        echo "Displaying index.html content:"
                        cat index.html
                    '''
                }
            }
        }

        stage('Copy File to Remote Server') {
            steps {
                sshagent(['aws-personal']) {
                    script {
                        // Copy the index.html file to the /tmp directory on the remote server
                        sh '''
                            echo "Copying index.html to remote server /tmp location..."
                            scp -o StrictHostKeyChecking=no index.html ubuntu@172.31.82.42:/tmp/
                        '''
                    }
                }
            }
        }

        stage('Move File on Remote Server') {
            steps {
                sshagent(['aws-personal']) {
                    script {
                        // Use sudo -i bash -c to get root access and move the file
                        sh '''
                            echo "Moving index.html to /var/www/html on the remote server..."
                            ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 \
                            'sudo -i bash -c "mv /tmp/index.html /var/www/html/index.html"'
                        '''
                    }
                }
            }
        }

        stage('Reload Apache Service') {
            steps {
                sshagent(['aws-personal']) {
                    script {
                        // Use sudo -i bash -c to reload the Apache service
                        sh '''
                            echo "Reloading Apache service on the remote server..."
                            ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 \
                            'sudo -i bash -c "systemctl reload apache2.service"'
                        '''
                    }
                }
            }
        }
    }
}
