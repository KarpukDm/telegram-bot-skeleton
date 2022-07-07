IMAGE:=tgbot-skeleton
DOCKER_REPO:=karpukdm/tgbot-skeleton
VERSION:=$(shell git rev-parse HEAD)

build-image:
	gradle build
	docker build \
		--build-arg JAR_FILE=build/libs/*.jar \
		-t ${IMAGE} .

run-image:
	docker run \
	--network=host \
	--env TG_BOT_TOKEN=${TG_BOT_TOKEN} \
	--env TG_BOT_NAME=${TG_BOT_NAME} \
	-d ${IMAGE}

publish:
	docker push $(IMAGE)
	docker tag $(IMAGE) $(DOCKER_REPO):$(VERSION)
	docker push $(IMAGE):$(VERSION)