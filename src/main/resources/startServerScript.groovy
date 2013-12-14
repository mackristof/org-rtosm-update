import org.vertx.groovy.core.eventbus.EventBus

/**
 * Init Server
 * Created by mackristof
 */

// File Service
container.deployVerticle("groovy:org.rtosm.file.FileFinderVerticle")
// Details downloader Verticle
container.deployVerticle("groovy:org.rtosm.loader.DownloadChangeSetVerticle")
// "Is there ChangeSet" Verticle
container.deployVerticle("groovy:org.rtosm.loader.ChangesetLoaderVerticle")
// Time orchetration 
container.deployVerticle("groovy:org.rtosm.timer.ChangeSetsTimer")

// start HTTP serveur
def serverHTTP = vertx.createHttpServer()

serverHTTP.requestHandler { request ->
	container.logger.info("requested file : $request.uri")
	vertx.eventBus.send("find-page",request.uri){ message ->
		request.response.end(message.body)
	}
}.listen(8000, "localhost")
def serverSock = vertx.createHttpServer()
vertx.createSockJSServer(serverSock).bridge(prefix: '/eventbus', [[:]], [[:]])
serverSock.listen(9090) 

