# TODO - set your deployment directory, the user that will run your app, and your app's name
ROOT="/opt/your_app_dir"
USER="OS user who will run your app"
APP_NAME="Your App Name"
ARTIFACT="${ARTIFACT_FINAL_NAME}.war" # This can be set by maven during a build or provided as an env prop

YELLOW="\e[0;93m"
MAGENTA="\e[0;95m"
CYAN="\e[0;96m"
BOLD_GREEN="\e[1;92m"
BOLD_WHITE="\e[1;97m"
RESET="\e[0m"

printf "\n\n${BOLD_WHITE}Deploying ${APP_NAME}${RESET}\n\n"

if [[ ! -d $ROOT ]]
then
  printf "Creating application directory ${CYAN}${ROOT}/${RESET}...\n"
  sudo mkdir $ROOT
fi

printf "Moving ${YELLOW}${ARTIFACT}${RESET} to ${CYAN}${ROOT}/${RESET}...\n"
sudo mv ~/${ARTIFACT} $ROOT

printf "Moving ${YELLOW}maintenance.html${RESET} to ${CYAN}${ROOT}/${RESET}...\n"
sudo mv ~/maintenance.html $ROOT

printf "Moving ${YELLOW}run.sh${RESET} to ${CYAN}${ROOT}/${RESET}...\n"
sudo mv ~/run.sh $ROOT

printf "Setting ${YELLOW}run.sh${RESET} as executable...\n"
sudo chmod u+x $ROOT/run.sh

if [[ ! -d $ROOT/logs ]]
then
  printf "Creating directory ${CYAN}${ROOT}/logs/${RESET}...\n"
  sudo mkdir $ROOT/logs
fi

if ! id -u "$USER" >/dev/null 2>&1
then
  printf "Creating user ${MAGENTA}${USER}${RESET}...\n"
  if [[ -d /home/${USER} ]]
  then
    printf "Existing home directory found for ${MAGENTA}${USER}${RESET}; removing...\n"
    sudo rm -rf /home/${USER}
  fi
  sudo useradd $USER
  sudo sh -c "echo -e '\ncd ${ROOT}' >> /home/${USER}/.bashrc"
  printf "\nRun ${YELLOW}aws configure${RESET} as ${MAGENTA}${USER}${RESET} to add this user's ${BOLD_WHITE}Secrets Manager${RESET} key\n\n"
fi

printf "Changing ownership of ${CYAN}${ROOT}${RESET} to user ${MAGENTA}${USER}${RESET}...\n"
sudo chown -R $USER $ROOT

printf "\n${BOLD_GREEN}${APP_NAME} successfully deployed.${RESET}\n"
printf "To run, execute ${YELLOW}run.sh${RESET} from ${CYAN}${ROOT}${RESET} as user ${MAGENTA}${USER}${RESET}\n\n\n"