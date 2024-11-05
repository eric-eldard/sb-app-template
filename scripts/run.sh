# TODO - set the user that will run your app and your app's name
USER="OS user who will run your app"
APP_NAME="Your App Name"
ARTIFACT="${ARTIFACT_FINAL_NAME}.war" # This can be set by maven during a build or provided as an env prop

YELLOW="\e[0;93m"
WHITE="\e[0;97m"
BOLD_GREEN="\e[1;92m"
BOLD_WHITE="\e[1;97m"
RESET="\e[0m"

printf "\n\n${BOLD_WHITE}Starting ${APP_NAME}${RESET}\n\n"

printf "Killing any process already running on ${WHITE}:8080${RESET}...\n"
fuser -k 8080/tcp

# TODO - set AWS Secrets Manager --secret-id values for database creds and JWT signing key
printf "Fetching ${WHITE}database credentials${RESET}...\n"
json=`aws secretsmanager get-secret-value --secret-id app-rds-access --query SecretString --output text`
host=`echo $json | jq -r ".host"`
port=`echo $json | jq -r ".port"`
db=`echo $json | jq -r ".dbInstanceIdentifier"`
export SPRING_DATASOURCE_URL="jdbc:mysql://${host}:${port}/${db}"
export SPRING_DATASOURCE_USERNAME=`echo $json | jq -r ".username"`
export SPRING_DATASOURCE_PASSWORD=`echo $json | jq -r ".password"`

printf "Fetching ${WHITE}JWT signing key${RESET}...\n"
json=`aws secretsmanager get-secret-value --secret-id jwt-signing-key --query SecretString --output text`
export JWT_SIGNING_KEY=`echo $json | jq -r ".key"`

printf "Starting ${YELLOW}${ARTIFACT}${RESET}...\n"
nohup java --enable-preview -jar ${ARTIFACT} > logs/output.log 2>&1 &

printf "\n${BOLD_GREEN}${APP_NAME} has started.${RESET}\n"
printf "It may take up to ${WHITE}30 seconds for Spring${RESET} to serve requests\n\n\n"