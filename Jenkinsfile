#!/usr/bin/env groovy

timestamps {
    ansiColor('xterm') {
        node {
            stage('Setup') {
                checkout scm
            }

            stage('Build') {
                try {
                    sh './mvnw verify'
                    archiveArtifacts '**/target/*.zip'
                } finally {
                    junit '**/target/surefire-reports/*.xml'
                }
            }

        }
    }
}
