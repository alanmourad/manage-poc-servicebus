package com.example.demo;

import java.util.Arrays;
import java.util.List;
import com.azure.messaging.servicebus.*;

public class DemoApplication {

	static String connectionString = "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";

	static String queueName = "queue.1";

	public static void main(String[] args) throws InterruptedException {
		sendMessage();
		//sendMessageBatch();
		
	}

	static void sendMessage() {
		// create a Service Bus Sender client for the queue
		ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
				.connectionString(connectionString.toString())
				.sender()
				.queueName(queueName)
				.buildClient();

			
				

		// send one message to the queue
		senderClient.sendMessage(new ServiceBusMessage("Hello, World!"));
		System.out.println("Sent a single message to the queue: " + queueName);
	}

	static List<ServiceBusMessage> createMessages() {
		// create a list of messages and return it to the caller
		ServiceBusMessage[] messages = {
				new ServiceBusMessage("First message"),
				new ServiceBusMessage("Second message"),
				new ServiceBusMessage("Third message")
		};
		return Arrays.asList(messages);
	}

	static void sendMessageBatch() {
		// create a Service Bus Sender client for the queue
		ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
				.connectionString(connectionString)
				.sender()
				.queueName(queueName)
				.buildClient();

		// Creates an ServiceBusMessageBatch where the ServiceBus.
		ServiceBusMessageBatch messageBatch = senderClient.createMessageBatch();

		// create a list of messages
		List<ServiceBusMessage> listOfMessages = createMessages();

		// We try to add as many messages as a batch can fit based on the maximum size
		// and send to Service Bus when
		// the batch can hold no more messages. Create a new batch for next set of
		// messages and repeat until all
		// messages are sent.
		for (ServiceBusMessage message : listOfMessages) {
			if (messageBatch.tryAddMessage(message)) {
				continue;
			}

			// The batch is full, so we create a new batch and send the batch.
			senderClient.sendMessages(messageBatch);
			System.out.println("Sent a batch of messages to the queue: " + queueName);

			// create a new batch
			messageBatch = senderClient.createMessageBatch();

			// Add that message that we couldn't before.
			if (!messageBatch.tryAddMessage(message)) {
				System.err.printf("Message is too large for an empty batch. Skipping. Max size: %s.",
						messageBatch.getMaxSizeInBytes());
			}
		}

		if (messageBatch.getCount() > 0) {
			senderClient.sendMessages(messageBatch);
			System.out.println("Sent a batch of messages to the queue: " + queueName);
		}

		// close the client
		senderClient.close();
	}

}