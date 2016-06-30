install_deps()
{
	apt-get -qq -y update
	apt-get -qq -y install curl

	apt-get -y install openjdk-7-jdk
}
