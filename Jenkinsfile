pipeline {
    agent { docker { image 'gradle:6.3.0-jdk11' } }
    
    environment {
        USERNAME = credentials('marvin-github-username');
        TOKEN = credentials('marvin-github-token')
    }

    stages {
        stage('build') {
            steps {
                sh 'gradle uberJar'
            }
        }
    }
}
