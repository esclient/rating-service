-include .env

PROTO_TAG ?= v0.0.12
PROTO_NAME := rating.proto

TMP_DIR := .proto
OUT_DIR := src/rating-service/grpc

.PHONY: clean fetch-proto get-stubs update format lint test

ifeq ($(OS),Windows_NT)
MKDIR	 = powershell -Command "New_Item -ItemType Directory -Force -Path"
RM		 = powershell -NoProfile -Command "Remove-Item -Path '$(TMP_DIR)' -Recurse -Force"
DOWN	 = powershell -Command "Invoke-WebrRequest -Uri"
DOWN_OUT = -OutFile
else
MKDIR 	 = mkdir -p
RM	  	 = rm -rf $(TMP_DIR)
DOWN  	 = wget
DOWN_OUT = -O
endif


docker-build:
		docker build --build-arg PORT=$(PORT) -t mod .

run: docker-build 
		docker run --rm -it \
				--env-file .env \
				-p $(PORT):$(PORT) \
				-v $(CURDIR):/app \
				-e WATCHFILES_FORCE_POLLING=true \
				mod

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

update: get-stubs clean