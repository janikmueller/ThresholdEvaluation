# ThresholdEvaluation
This project builds a native image. This native image returns which thresholds are violated.

## Native Image Usage
**The following parameters are required:**
- base-url: the url to access the teamscale instance
- project: the project name which is already created in teamscale and gets evaluated
- branch: the branch name
- threshold-configuration: the threshold configuration profile defining which metrics are evaluated with the corresponding threshold value

**Optional parameters:**
- --login <username> <password>: if a login is required to access the teamscale instance, set the --login flag followed by the username and password
- --fail-on-yellow: set this flag, if a violation of the 'YELLOW' threshold value should be printed and cause a non-zero exit code as well (without the flag only violations of the 'RED' threshold value are printed and cause a non-zero exit code)
  
Running the native image only with --help/--version returns the help message/version of the native image.

## Examples
```
./ThresholdEvaluationNativeImage http://localhost:8080/ MyProject master 'Project Default' --login admin admin --fail-on-yellow
```
```
./ThresholdEvaluationNativeImage --help
```

## Project Usage

**Prerequisites**
- install graalvm (https://www.graalvm.org/getting-started/)
- install native-image extension of graalvm
- install maven (https://maven.apache.org/install.html)

Configure your toolchain.xml:
- if you don't have a toolchain.xml yet, create the file at ~/.m2
- add the following code to your toolchain.xml, exchanging < path-to-java-graalvm-jdk > with the actual path:
```
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
        <version>1.8</version>
        <graalVmVersion>20.1.0</graalVmVersion>
    </provides>
    <configuration>
        <jdkHome><path-to-java-graalvm-jdk></jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

In order to modify the ThresholdEvaluation.java file and then build a new native image from it, follow these steps:
- make your changes to ```~/ThresholdEvaluation/thresholdevaluation/src/main/java/com.cqse.thresholdevaluation/ThresholdEvaluation.java```
- open the terminal/command prompt and navigate to ```~/ThresholdEvaluation/thresholdevaluation/```
- run ```mvn package```
- if build was successful, the new native image is located at ```~/ThresholdEvaluation/thresholdevaluation/target```
