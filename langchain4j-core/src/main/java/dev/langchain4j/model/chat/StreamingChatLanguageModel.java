package dev.langchain4j.model.chat;

import dev.langchain4j.Experimental;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;

import java.util.List;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static java.util.Collections.singletonList;

/**
 * TODO review all javadoc in this class
 * Represents a language model that has a chat interface and can stream a response one token at a time.
 *
 * @see ChatLanguageModel
 */
public interface StreamingChatLanguageModel {

    /**
     * TODO
     * <p>
     * A temporary default implementation of this method is necessary
     * until all {@link StreamingChatLanguageModel} implementations adopt it. It should be removed once that occurs.
     *
     * @param chatRequest
     * @param handler
     */
    @Experimental
    default void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {

        ResponseFormat responseFormat = chatRequest.responseFormat();
        if (responseFormat != null && responseFormat.type() == ResponseFormatType.JSON) {
            throw new UnsupportedOperationException("JSON response type is not supported by this model provider");
        }

        StreamingResponseHandler<AiMessage> legacyHandler = new StreamingResponseHandler<>() {

            @Override
            public void onNext(String token) {
                handler.onNext(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                ChatResponse chatResponse = ChatResponse.builder()
                        .aiMessage(response.content())
                        .tokenUsage(response.tokenUsage())
                        .finishReason(response.finishReason())
                        .build();
                handler.onComplete(chatResponse);
            }

            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        };

        if (isNullOrEmpty(chatRequest.toolSpecifications())) {
            generate(chatRequest.messages(), legacyHandler);
        } else {
            generate(chatRequest.messages(), chatRequest.toolSpecifications(), legacyHandler);
        }
    }

    // TODO add convenience method(s)? e.g. chat(String, StreamingChatResponseHandler)

    // TODO API for N completions?

    /**
     * Generates a response from the model based on a message from a user.
     *
     * @param userMessage The message from the user.
     * @param handler     The handler for streaming the response.
     */
    @Deprecated(forRemoval = true) // TODO
    default void generate(String userMessage, StreamingResponseHandler<AiMessage> handler) {
        generate(singletonList(UserMessage.from(userMessage)), handler);
    }

    /**
     * Generates a response from the model based on a message from a user.
     *
     * @param userMessage The message from the user.
     * @param handler     The handler for streaming the response.
     */
    @Deprecated(forRemoval = true) // TODO
    default void generate(UserMessage userMessage, StreamingResponseHandler<AiMessage> handler) {
        generate(singletonList(userMessage), handler);
    }

    /**
     * Generates a response from the model based on a sequence of messages.
     * Typically, the sequence contains messages in the following order:
     * System (optional) - User - AI - User - AI - User ...
     *
     * @param messages A list of messages.
     * @param handler  The handler for streaming the response.
     */
    @Deprecated(forRemoval = true)
    // TODO
    void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler);

    /**
     * Generates a response from the model based on a list of messages and a list of tool specifications.
     * The response may either be a text message or a request to execute one of the specified tools.
     * Typically, the list contains messages in the following order:
     * System (optional) - User - AI - User - AI - User ...
     *
     * @param messages           A list of messages.
     * @param toolSpecifications A list of tools that the model is allowed to execute.
     *                           The model autonomously decides whether to use any of these tools.
     * @param handler            The handler for streaming the response.
     *                           {@link AiMessage} can contain either a textual response or a request to execute one of the tools.
     */
    @Deprecated(forRemoval = true) // TODO
    default void generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications, StreamingResponseHandler<AiMessage> handler) {
        throw new IllegalArgumentException("Tools are currently not supported by this model");
    }

    /**
     * Generates a response from the model based on a list of messages and a tool specification.
     *
     * @param messages          A list of messages.
     * @param toolSpecification A tool that the model is allowed to execute.
     * @param handler           The handler for streaming the response.
     */
    @Deprecated(forRemoval = true) // TODO
    default void generate(List<ChatMessage> messages, ToolSpecification toolSpecification, StreamingResponseHandler<AiMessage> handler) {
        throw new IllegalArgumentException("Tools are currently not supported by this model");
    }
}