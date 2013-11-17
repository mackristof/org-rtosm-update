package org.rtosm.test.loader;

import java.util.List;

import org.junit.Test;
import org.rtosm.loader.ChangesetLoaderVerticle;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ChangesetLoaderVerticleTest extends TestVerticle {

	@Test
	public void testVerticle() {
		vertx.eventBus().send("load-changesets", "",
				new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> msg) {
						container.logger().info(
								"msg received = " + msg.body());
						assertTrue(msg.body().getElement("ids").asArray().size()>0);
						testComplete();
					}
				});

	}

	@Override
	public void start() {
		initialize();

		container.deployVerticle(
				"groovy:" + ChangesetLoaderVerticle.class.getName(),
				new Handler<AsyncResult<String>>() {

					@Override
					public void handle(AsyncResult<String> asyncResult) {
						// Deployment is asynchronous and this this handler will
						// be called when it's complete (or failed)
						if (asyncResult.failed()) {
							container.logger().error(asyncResult.cause());
						}
						assertTrue(asyncResult.succeeded());
						assertNotNull("deploymentID should not be null",
								asyncResult.result());
						// If deployed correctly then start the tests!
						startTests();
					}
				});

	}
}