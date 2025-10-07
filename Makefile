-include .env
PROTO_TAG ?= v0.1.1
PROTO_NAME := rating.proto
TMP_DIR := .proto
OUT_DIR := src/main/java
DOCKER   ?= docker

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
	docker build --pull --no-cache --build-arg PORT=$(PORT) -t rating .

run: docker-build
	docker run --rm -it \
		--env-file .env \
		-p $(PORT):$(PORT) \
		rating

clean:
	$(RM)

fetch-proto:
	$(MKDIR) "$(TMP_DIR)"
	$(DOWN) "https://raw.githubusercontent.com/esclient/protos/$(PROTO_TAG)/$(PROTO_NAME)" $(DOWN_OUT) "$(TMP_DIR)/$(PROTO_NAME)"

gen-stubs: fetch-proto
	$(MKDIR) "$(OUT_DIR)"
	protoc \
		--proto_path="$(TMP_DIR)" \
		--java_out="$(OUT_DIR)" \
		--grpc-java_out="$(OUT_DIR)" \
		"$(TMP_DIR)/$(PROTO_NAME)"

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
