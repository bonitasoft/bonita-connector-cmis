#CMIS Connector

Enables interactions in your BonitaBPM processes with a CMIS compatible CMS.

##Description

This connector provides the following actions:
	 * _Create a folder_
	 * _Delete a folder_
	 * _Upload a new document_
	 * _Upload a list of new documents_
	 * _Upload a new version of a document_
	 * _Download a document_
	 * _Delete a document_
	 * _List documents_
	 * _List documents_

##Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`mvn clean verify`

##Release

Before releasing a new version make sure that the versionning of `.def` files are consistent with the implementation. In order to create a new release of the connector use the [_maven release plugin_](http://maven.apache.org/maven-release/maven-release-plugin/):

`mvn release:prepare`

It creates a new git _tag_ and update the current development version. New tags are automatically built on our CI infrastructure.
