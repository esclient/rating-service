-include .env
PROTO_TAG ?= v0.0.14
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
	docker build --pull --no-cache --build-arg PORT=$(PORT) -t rating .


run: update docker-build
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

# Formatting - applies code formatting automatically
format:
	mvn spotless:apply -Pquality 

# Linting - runs all quality checks (explicit individual commands)
lint:
	mvn spotless:check -Pquality 
	mvn checkstyle:check -Pquality 
	mvn pmd:check -Pquality 
	mvn spotbugs:check -Pquality 

# Show detailed linting errors (verbose mode)
lint-verbose:
	mvn spotless:check -Pquality
	mvn checkstyle:check -Pquality
	mvn pmd:check -Pquality
	mvn spotbugs:check -Pquality

# Test with coverage report
test:
	mvn test jacoco:report -Pquality

# Development workflow - format, lint, then test
dev-check: format lint test
	@echo "All checks passed! ✅"
