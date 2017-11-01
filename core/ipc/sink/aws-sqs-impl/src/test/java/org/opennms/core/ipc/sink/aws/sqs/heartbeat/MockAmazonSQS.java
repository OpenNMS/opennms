/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.core.ipc.sink.aws.sqs.heartbeat;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AddPermissionRequest;
import com.amazonaws.services.sqs.model.AddPermissionResult;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchResult;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityResult;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.DeleteQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ListDeadLetterSourceQueuesRequest;
import com.amazonaws.services.sqs.model.ListDeadLetterSourceQueuesResult;
import com.amazonaws.services.sqs.model.ListQueueTagsRequest;
import com.amazonaws.services.sqs.model.ListQueueTagsResult;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.PurgeQueueResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.RemovePermissionRequest;
import com.amazonaws.services.sqs.model.RemovePermissionResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesResult;
import com.amazonaws.services.sqs.model.TagQueueRequest;
import com.amazonaws.services.sqs.model.TagQueueResult;
import com.amazonaws.services.sqs.model.UntagQueueRequest;
import com.amazonaws.services.sqs.model.UntagQueueResult;

/**
 * The Class MockAmazonSQS.
 * <p>This provides mock implementations for send, receive and delete messages, and get Queue URLs.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockAmazonSQS implements AmazonSQS {

    /** The messages. */
    private Hashtable<String, Message> messages = new Hashtable<>();

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public Hashtable<String, Message> getMessages() {
        return messages;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#setEndpoint(java.lang.String)
     */
    @Override
    public void setEndpoint(String endpoint) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#setRegion(com.amazonaws.regions.Region)
     */
    @Override
    public void setRegion(Region region) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#addPermission(com.amazonaws.services.sqs.model.AddPermissionRequest)
     */
    @Override
    public AddPermissionResult addPermission(
            AddPermissionRequest addPermissionRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#addPermission(java.lang.String, java.lang.String, java.util.List, java.util.List)
     */
    @Override
    public AddPermissionResult addPermission(String queueUrl, String label,
            List<String> aWSAccountIds, List<String> actions) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#changeMessageVisibility(com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest)
     */
    @Override
    public ChangeMessageVisibilityResult changeMessageVisibility(
            ChangeMessageVisibilityRequest changeMessageVisibilityRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#changeMessageVisibility(java.lang.String, java.lang.String, java.lang.Integer)
     */
    @Override
    public ChangeMessageVisibilityResult changeMessageVisibility(
            String queueUrl, String receiptHandle,
            Integer visibilityTimeout) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#changeMessageVisibilityBatch(com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequest)
     */
    @Override
    public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(
            ChangeMessageVisibilityBatchRequest changeMessageVisibilityBatchRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#changeMessageVisibilityBatch(java.lang.String, java.util.List)
     */
    @Override
    public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(
            String queueUrl,
            List<ChangeMessageVisibilityBatchRequestEntry> entries) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#createQueue(com.amazonaws.services.sqs.model.CreateQueueRequest)
     */
    @Override
    public CreateQueueResult createQueue(
            CreateQueueRequest createQueueRequest) {
        return createQueue(createQueueRequest.getQueueName());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#createQueue(java.lang.String)
     */
    @Override
    public CreateQueueResult createQueue(String queueName) {
        return new CreateQueueResult().withQueueUrl(getQueueUrl(queueName).getQueueUrl());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteMessage(com.amazonaws.services.sqs.model.DeleteMessageRequest)
     */
    @Override
    public DeleteMessageResult deleteMessage(
            DeleteMessageRequest deleteMessageRequest) {
        return deleteMessage(deleteMessageRequest.getQueueUrl(), deleteMessageRequest.getReceiptHandle());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteMessage(java.lang.String, java.lang.String)
     */
    @Override
    public DeleteMessageResult deleteMessage(String queueUrl,
            String receiptHandle) {
        if (messages.containsKey(receiptHandle))
            messages.remove(receiptHandle);
        return new DeleteMessageResult();
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteMessageBatch(com.amazonaws.services.sqs.model.DeleteMessageBatchRequest)
     */
    @Override
    public DeleteMessageBatchResult deleteMessageBatch(
            DeleteMessageBatchRequest deleteMessageBatchRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteMessageBatch(java.lang.String, java.util.List)
     */
    @Override
    public DeleteMessageBatchResult deleteMessageBatch(String queueUrl,
            List<DeleteMessageBatchRequestEntry> entries) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteQueue(com.amazonaws.services.sqs.model.DeleteQueueRequest)
     */
    @Override
    public DeleteQueueResult deleteQueue(
            DeleteQueueRequest deleteQueueRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#deleteQueue(java.lang.String)
     */
    @Override
    public DeleteQueueResult deleteQueue(String queueUrl) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#getQueueAttributes(com.amazonaws.services.sqs.model.GetQueueAttributesRequest)
     */
    @Override
    public GetQueueAttributesResult getQueueAttributes(
            GetQueueAttributesRequest getQueueAttributesRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#getQueueAttributes(java.lang.String, java.util.List)
     */
    @Override
    public GetQueueAttributesResult getQueueAttributes(String queueUrl,
            List<String> attributeNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#getQueueUrl(com.amazonaws.services.sqs.model.GetQueueUrlRequest)
     */
    @Override
    public GetQueueUrlResult getQueueUrl(
            GetQueueUrlRequest getQueueUrlRequest) {
        return getQueueUrl(getQueueUrlRequest.getQueueName());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#getQueueUrl(java.lang.String)
     */
    @Override
    public GetQueueUrlResult getQueueUrl(String queueName) {
        return new GetQueueUrlResult().withQueueUrl(MockAmazonSQSManager.URL_PREFIX + queueName);
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listDeadLetterSourceQueues(com.amazonaws.services.sqs.model.ListDeadLetterSourceQueuesRequest)
     */
    @Override
    public ListDeadLetterSourceQueuesResult listDeadLetterSourceQueues(
            ListDeadLetterSourceQueuesRequest listDeadLetterSourceQueuesRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listQueues(com.amazonaws.services.sqs.model.ListQueuesRequest)
     */
    @Override
    public ListQueuesResult listQueues(ListQueuesRequest listQueuesRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listQueues()
     */
    @Override
    public ListQueuesResult listQueues() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listQueues(java.lang.String)
     */
    @Override
    public ListQueuesResult listQueues(String queueNamePrefix) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#purgeQueue(com.amazonaws.services.sqs.model.PurgeQueueRequest)
     */
    @Override
    public PurgeQueueResult purgeQueue(PurgeQueueRequest purgeQueueRequest) {
        messages.clear();
        return new PurgeQueueResult();
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#receiveMessage(com.amazonaws.services.sqs.model.ReceiveMessageRequest)
     */
    @Override
    public ReceiveMessageResult receiveMessage(
            ReceiveMessageRequest receiveMessageRequest) {
        return receiveMessage(receiveMessageRequest.getQueueUrl());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#receiveMessage(java.lang.String)
     */
    @Override
    public ReceiveMessageResult receiveMessage(String queueUrl) {
        return new ReceiveMessageResult().withMessages(messages.values());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#removePermission(com.amazonaws.services.sqs.model.RemovePermissionRequest)
     */
    @Override
    public RemovePermissionResult removePermission(
            RemovePermissionRequest removePermissionRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#removePermission(java.lang.String, java.lang.String)
     */
    @Override
    public RemovePermissionResult removePermission(String queueUrl,
            String label) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#sendMessage(com.amazonaws.services.sqs.model.SendMessageRequest)
     */
    @Override
    public SendMessageResult sendMessage(
            SendMessageRequest sendMessageRequest) {
        return sendMessage(sendMessageRequest.getQueueUrl(), sendMessageRequest.getMessageBody());
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#sendMessage(java.lang.String, java.lang.String)
     */
    @Override
    public SendMessageResult sendMessage(String queueUrl,
            String messageBody) {
        String id = Long.toString(new Date().getTime());
        Message msg = new Message().withBody(messageBody).withMessageId(id).withReceiptHandle(id);
        messages.put(id, msg);
        return new SendMessageResult().withMessageId(id);
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#sendMessageBatch(com.amazonaws.services.sqs.model.SendMessageBatchRequest)
     */
    @Override
    public SendMessageBatchResult sendMessageBatch(
            SendMessageBatchRequest sendMessageBatchRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#sendMessageBatch(java.lang.String, java.util.List)
     */
    @Override
    public SendMessageBatchResult sendMessageBatch(String queueUrl,
            List<SendMessageBatchRequestEntry> entries) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#setQueueAttributes(com.amazonaws.services.sqs.model.SetQueueAttributesRequest)
     */
    @Override
    public SetQueueAttributesResult setQueueAttributes(
            SetQueueAttributesRequest setQueueAttributesRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#setQueueAttributes(java.lang.String, java.util.Map)
     */
    @Override
    public SetQueueAttributesResult setQueueAttributes(String queueUrl,
            Map<String, String> attributes) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#shutdown()
     */
    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
     */
    @Override
    public ResponseMetadata getCachedResponseMetadata(
            AmazonWebServiceRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listQueueTags(com.amazonaws.services.sqs.model.ListQueueTagsRequest)
     */
    @Override
    public ListQueueTagsResult listQueueTags(ListQueueTagsRequest arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#listQueueTags(java.lang.String)
     */
    @Override
    public ListQueueTagsResult listQueueTags(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#tagQueue(com.amazonaws.services.sqs.model.TagQueueRequest)
     */
    @Override
    public TagQueueResult tagQueue(TagQueueRequest arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#tagQueue(java.lang.String, java.util.Map)
     */
    @Override
    public TagQueueResult tagQueue(String arg0, Map<String, String> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#untagQueue(com.amazonaws.services.sqs.model.UntagQueueRequest)
     */
    @Override
    public UntagQueueResult untagQueue(UntagQueueRequest arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.sqs.AmazonSQS#untagQueue(java.lang.String, java.util.List)
     */
    @Override
    public UntagQueueResult untagQueue(String arg0, List<String> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

}
