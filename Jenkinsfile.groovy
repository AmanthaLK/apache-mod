pipeline {
    agent any

    stages {
        stage('Check Local File') {
            steps {
                script {
                    // Print the current directory and list files
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

        stage('Upload File to Remote Server') {
            steps {
                script {
                    sshagent(['aws-personal']) {
                        try {
                            // Copy the file to the remote server with root access
                            sh '''
                                echo "Uploading index.html to /var/www/html on remote server with root access..."
                                cat index.html | ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 "sudo tee /var/www/html/index.html > /dev/null"
                                sudo ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 "sudo chown www-data:www-data /var/www/html/index.html"
                                sudo ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 "sudo chmod 644 /var/www/html/index.html"
                            '''
                        } catch (Exception e) {
                            echo "Failed to upload index.html: ${e.message}"
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
        }

        stage('Verify Remote File') {
            steps {
                script {
                    sshagent(['aws-personal']) {
                        try {
                            sh '''
                                echo "Verifying file on remote server..."
                                ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 << 'EOF'
                                sudo -i bash -c '
                                    echo "Files in /var/www/html:";
                                    ls -l /var/www/html;
                                    echo "Content of index.html:";
                                    cat /var/www/html/index.html;
                                '
                                exit
                                EOF
                            '''
                        } catch (Exception e) {
                            echo "SSH connection or file verification failed: ${e.message}"
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
        }
    }
}
