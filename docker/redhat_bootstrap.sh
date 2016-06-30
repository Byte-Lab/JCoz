install_deps()
{
	yum -y update 
	yum -y install curl
	yum -y install java
	yum install -y java-1.8.0-openjdk-devel
}

