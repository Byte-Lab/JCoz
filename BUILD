#####################################
#          Native Targets           #
#####################################
COPTS = [
   
]
cc_library(
   name = "jcoz_agent",
   srcs = glob(["src/native/*.cc", "src/native/*.h"]),
   # includes = ["src/spdlog", "/usr/lib/jvm/java-8-openjdk/include",
   includes = ["src/spdlog", "$(JAVABASE)/include"],
   copts = ["
)

#####################################
#           Java Targets            #
#####################################

# RMI Server that listens for connections and connects to running profiled JCoz processes.
java_binary(
   name = "rmi_server",
   main_class = "com.vernetperronllc.jcoz.service.JCozService",
   srcs = glob(["src/java/src/main/java/com/vernetperronllc/jcoz/service/*.java"]),
   deps = [":jcoz_jar", ":static_jar_imports"]
)

# UI Client that connects to RMI service to profile processes and display results on a graphical frontend.
java_binary(
   name = "client_ui",
   main_class = "com.vernetperronllc.jcoz.client.ui.JCozClientUI",
   srcs = glob(["src/java/src/main/java/com/vernetperronllc/jcoz/client/ui/*.java"]),
   deps = [":jcoz_jar", ":static_jar_imports"]
)

# UI Client that connects to RMI service to profile processes and display results on a graphical frontend.
java_binary(
   name = "client_cli",
   main_class = "com.vernetperronllc.jcoz.client.cli.JCozCLI",
   srcs = glob(["src/java/src/main/java/com/vernetperronllc/jcoz/client/cli/*.java"]),
   deps = [":jcoz_jar", ":static_jar_imports", "@commons_cli//jar"]
)

# Creates the JAR file that is shared by JCoz executables.
java_library(
   name = "jcoz_jar",
   deps = [":static_jar_imports", "@commons_cli//jar"],
   srcs = glob([
         "src/java/src/main/java/com/vernetperronllc/jcoz/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/agent/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/client/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/client/cli/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/client/ui/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/profile/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/profile/sort/*.java",
         "src/java/src/main/java/com/vernetperronllc/jcoz/service/*.java",
   ])
)

# Import the static JAR dependencies that are needed by the JCoz jar.
# Only JAR files that are not versioned in some official repository
# (e.g. Maven, GitHub, etc) should be placed in this directory. Other
# external dependencies should be listed in WORKSPACE and referenced
# in the same way as @commons_cli//jar above.
java_import(
   name = "static_jar_imports",
   jars = glob([ "static_jar_deps/*.jar"])
)


