pipeline {
    agent { docker { image 'gradle:6.3.0-jdk8' } }
    stages {
        stage('build') {
            steps {
                sh 'gradle --version'
            }
        }
    }
}
