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
                script {
                    // Copy the index.html file to the /tmp directory on the remote server
                    sh '''
                        echo "Copying index.html to remote server /tmp location..."
                        scp -i /path/to/private_key -o StrictHostKeyChecking=no index.html ubuntu@172.31.82.42:/tmp/
                    '''
                }
            }
        }
    }
}
