-include .env
SERVER_PORT ?= 8080
PROTO_TAG ?= v0.0.12
PROTO_NAME := rating.proto
TMP_DIR := .proto
OUT_DIR := src/main/java

.PHONY: clean fetch-proto get-stubs update format lint test docker-build run run-dev

ifeq ($(OS),Windows_NT)
MKDIR    = powershell -Command "New-Item -ItemType Directory -Force -Path"
RM       = powershell -NoProfile -Command "Remove-Item -Path '$(TMP_DIR)' -Recurse -Force"
DOWN     = powershell -Command "Invoke-WebRequest -Uri"
DOWN_OUT = -OutFile
else
MKDIR    = mkdir -p
RM       = rm -rf $(TMP_DIR)
DOWN     = wget
DOWN_OUT = -O
endif

docker-build:
	docker build --pull --no-cache --build-arg PORT=$(SERVER_PORT) -t rating .

# Production run
run: update docker-build
	docker run --rm -it \
		--env-file .env \
		-e DB_URL \
		-e DB_USERNAME \
		-e DB_PASSWORD \
		-e SERVER_PORT \
		-p $(SERVER_PORT):$(SERVER_PORT) \
		rating


clean:
	$(RM)

fetch-proto:
	$(MKDIR) "$(TMP_DIR)"
	$(DOWN) "https://raw.githubusercontent.com/esclient/protos/$(PROTO_TAG)/$(PROTO_NAME)" $(DOWN_OUT) "$(TMP_DIR)/$(PROTO_NAME)"

get-stubs: fetch-proto
	$(MKDIR) "$(OUT_DIR)"
	protoc \
		--proto_path="$(TMP_DIR)" \
		--java_out="$(OUT_DIR)" \
		--grpc-java_out="$(OUT_DIR)" \
		"$(TMP_DIR)/$(PROTO_NAME)"

# Updated so we don't delete generated stubs
update: clean get-stubs
