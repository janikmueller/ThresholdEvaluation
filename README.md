# ThresholdEvaluation
This project builds a native image. This native image returns which thresholds are violated.


The following parameters are required:
- base-url: the url to access the teamscale instance
- project: the project name which is already created in teamscale and gets evaluated
- branch: the branch name
- threshold-configuration: the threshold configuration profile defining which metrics are evaluated with the corresponding threshold value

Optional parameters:
- --login <username> <password>: if a login is required to access the teamscale instance, set the --login flag followed by the username and password
- --fail-on-yellow: set this flag, if a violation of the 'YELLOW' threshold value should be printed and cause a non-zero exit code as well (without the flag only violations of the 'RED' threshold value are printed and cause a non-zero exit code)
  
Running the native image only with --help/--version returns the help message/version of the native image.


Example:

./thresholdevaluation http://localhost:8080/ MyProject master 'Project Default' --login admin admin --fail-on-yellow

./thresholdevaluation --help
