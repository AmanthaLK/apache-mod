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

        // Additional stages will be added here
    }
}
