pipeline {
    agent { docker { image 'gradle:6.3.0-jdk11' } }
    stages {
        stage('build') {
            steps {
                sh 'gradle uberJar'
            }
        }
    }
}
