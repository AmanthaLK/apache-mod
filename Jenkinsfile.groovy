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
                            // Copy the local index.html file to the remote server
                            sh '''
                                echo "Copying index.html to remote server..."
                                scp -o StrictHostKeyChecking=no index.html ubuntu@172.31.82.42:/var/www/html
                            '''
                        } catch (Exception e) {
                            echo "Failed to copy index.html: ${e.message}"
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
