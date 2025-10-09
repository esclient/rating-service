set dotenv-load

PROTO_TAG := env_var_or_default('PROTO_TAG', 'v0.1.1')
PROTO_NAME := "rating.proto"
TMP_DIR := ".proto"
OUT_DIR := "src/main/java"
DOCKER := env_var_or_default('DOCKER', 'docker')
PORT := env_var_or_default('PORT', '8080')

MKDIR := if os() == "windows" { "powershell -Command \"New-Item -ItemType Directory -Force -Path\"" } else { "mkdir -p" }
RM := if os() == "windows" { "powershell -NoProfile -Command \"Remove-Item -Path '" + TMP_DIR + "' -Recurse -Force\"" } else { "rm -rf " + TMP_DIR }
DOWN := if os() == "windows" { "powershell -Command \"Invoke-WebRequest -Uri\"" } else { "wget" }
DOWN_OUT := if os() == "windows" { "-OutFile" } else { "-O" }

docker-build:
    {{DOCKER}} build --pull --no-cache --build-arg PORT={{PORT}} -t rating .

run: docker-build
    {{DOCKER}} run --rm -it \
        --env-file .env \
        -p {{PORT}}:{{PORT}} \
        rating

clean:
    {{RM}}

fetch-proto:
    {{MKDIR}} "{{TMP_DIR}}"
    {{DOWN}} "https://raw.githubusercontent.com/esclient/protos/{{PROTO_TAG}}/{{PROTO_NAME}}" {{DOWN_OUT}} "{{TMP_DIR}}/{{PROTO_NAME}}"

gen-stubs: fetch-proto
    {{MKDIR}} "{{OUT_DIR}}"
    protoc \
        --proto_path="{{TMP_DIR}}" \
        --java_out="{{OUT_DIR}}" \
        --grpc-java_out="{{OUT_DIR}}" \
        "{{TMP_DIR}}/{{PROTO_NAME}}"

update: gen-stubs clean

format:
    mvn spotless:apply -Pquality
    @echo "-------------------------------"
    @echo "spotless formatting applied! ✅"
    @echo "-------------------------------"

lint:
    mvn spotless:check -Pquality
    @echo "-------------------------------"
    @echo "spotless check done! ✅"
    @echo "-------------------------------"
    mvn checkstyle:check -Pquality
    @echo "-------------------------------"
    @echo "checkstyle check done! ✅"
    @echo "-------------------------------"
    mvn spotbugs:check -Pquality
    @echo "-------------------------------"
    @echo "spotbugs check done! ✅"
    @echo "-------------------------------"
    mvn modernizer:modernizer -Pquality
    @echo "-------------------------------"
    @echo "modernizer check done! ✅"
    @echo "-------------------------------"
    @echo "Linting done! ✅"
    @echo "-------------------------------"

test:
    mvn test jacoco:report -Pquality
    @echo "-------------------------------"
    @echo "jacoco testing done! ✅"
    @echo "-------------------------------"

dev-check: format lint test
    @echo "---------------------------------"
    @echo "All checks passed! ✅"
    @echo "---------------------------------"

migrate:
    export INFISICAL_TOKEN="${INFISICAL_SERVER_TOKEN}" && bash scripts/run-migrations.sh