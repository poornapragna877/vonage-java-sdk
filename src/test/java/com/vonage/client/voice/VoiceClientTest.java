/*
 *   Copyright 2020 Vonage
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vonage.client.voice;

import com.vonage.client.HttpWrapper;
import com.vonage.client.TestUtils;
import com.vonage.client.auth.AuthCollection;
import com.vonage.client.auth.JWTAuthMethod;
import com.vonage.client.logging.LoggingUtils;
import com.vonage.client.voice.ncco.Ncco;
import com.vonage.client.voice.ncco.TalkAction;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggingUtils.class)
public class VoiceClientTest {
    private TestUtils testUtils = new TestUtils();

    private HttpWrapper stubHttpWrapper(int statusCode, String content) throws Exception {
        HttpClient client = mock(HttpClient.class);
        mockStatic(LoggingUtils.class);

        HttpResponse response = mock(HttpResponse.class);
        StatusLine sl = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);

        when(client.execute(any(HttpUriRequest.class))).thenReturn(response);
        when(LoggingUtils.logResponse(any(HttpResponse.class))).thenReturn("response logged");
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        when(sl.getStatusCode()).thenReturn(statusCode);
        when(response.getStatusLine()).thenReturn(sl);
        when(response.getEntity()).thenReturn(entity);

        byte[] keyBytes = testUtils.loadKey("test/keys/application_key");
        AuthCollection authCollection = new AuthCollection();
        authCollection.add(new JWTAuthMethod("951614e0-eec4-4087-a6b1-3f4c2f169cb0", keyBytes));

        HttpWrapper wrapper = new HttpWrapper(authCollection);
        wrapper.setHttpClient(client);

        return wrapper;
    }

    @Test
    public void testCreateCall() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"conversation_uuid\": \"63f61863-4a51-4f6b-86e1-46edebio0391\",\n"
                        + "  \"status\": \"started\",\n" + "  \"direction\": \"outbound\"\n" + "}"
        ));
        CallEvent evt = client.createCall(new Call("447700900903", "447700900904", "http://api.example.com/answer"));
        assertEquals("63f61863-4a51-4f6b-86e1-46edebio0391", evt.getConversationUuid());
    }

    @Test
    public void testListCallsNoFilter() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"page_size\": 10,\n" + "  \"record_index\": 0,\n" + "  \"count\": 0,\n"
                        + "  \"_embedded\": {\n" + "    \"calls\": []\n" + "  },\n" + "  \"_links\": {\n"
                        + "    \"self\": {\n" + "      \"href\": \"/v1/calls?page_size=10&record_index=0\"\n"
                        + "    },\n" + "    \"first\": {\n" + "      \"href\": \"/v1/calls?page_size=10\"\n"
                        + "    },\n" + "    \"last\": {\n" + "      \"href\": \"/v1/calls?page_size=10\"\n" + "    }\n"
                        + "  }\n" + "}\n"
        ));
        CallInfoPage page = client.listCalls();
        assertEquals(0, page.getCount());
    }

    @Test
    public void testListCallsWithFilter() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"page_size\": 10,\n" + "  \"record_index\": 0,\n" + "  \"count\": 0,\n"
                        + "  \"_embedded\": {\n" + "    \"calls\": []\n" + "  },\n" + "  \"_links\": {\n"
                        + "    \"self\": {\n" + "      \"href\": \"/v1/calls?page_size=10&record_index=0\"\n"
                        + "    },\n" + "    \"first\": {\n" + "      \"href\": \"/v1/calls?page_size=10\"\n"
                        + "    },\n" + "    \"last\": {\n" + "      \"href\": \"/v1/calls?page_size=10\"\n" + "    }\n"
                        + "  }\n" + "}\n"
        ));
        CallInfoPage page = client.listCalls(CallsFilter.builder().build());
        assertEquals(0, page.getCount());
    }

    @Test
    public void testGetCallDetails() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "      {\n" + "        \"uuid\": \"93137ee3-580e-45f7-a61a-e0b5716000ef\",\n"
                        + "        \"status\": \"completed\",\n" + "        \"direction\": \"outbound\",\n"
                        + "        \"rate\": \"0.02400000\",\n" + "        \"price\": \"0.00280000\",\n"
                        + "        \"duration\": \"7\",\n" + "        \"network\": \"23410\",\n"
                        + "        \"conversation_uuid\": \"aa17bd11-c895-4225-840d-30dc38c31e50\",\n"
                        + "        \"start_time\": \"2017-01-13T13:55:02.000Z\",\n"
                        + "        \"end_time\": \"2017-01-13T13:55:09.000Z\",\n" + "        \"to\": {\n"
                        + "          \"type\": \"phone\",\n" + "          \"number\": \"447700900104\"\n"
                        + "        },\n" + "        \"from\": {\n" + "          \"type\": \"phone\",\n"
                        + "          \"number\": \"447700900105\"\n" + "        },\n" + "        \"_links\": {\n"
                        + "          \"self\": {\n"
                        + "            \"href\": \"/v1/calls/93137ee3-580e-45f7-a61a-e0b5716000ef\"\n" + "          }\n"
                        + "        }\n" + "      }\n"
        ));
        CallInfo call = client.getCallDetails("93137ee3-580e-45f7-a61a-e0b5716000ef");
        assertEquals("93137ee3-580e-45f7-a61a-e0b5716000ef", call.getUuid());
    }

    @Test
    public void testSendDtmf() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"DTMF sent\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}"
        ));

        DtmfResponse response = client.sendDtmf("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", "332393");
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
        assertEquals("DTMF sent", response.getMessage());
    }

    @Test
    public void testModifyCall() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200, "{\"message\":\"Received\"}"));
        ModifyCallResponse call = client.modifyCall("93137ee3-580e-45f7-a61a-e0b5716000ef", ModifyCallAction.HANGUP);
        assertEquals("Received", call.getMessage());
    }

    @Test
    public void testModifyCall2() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200, "{\"message\":\"Received\"}"));
        ModifyCallResponse call = client.modifyCall(new CallModifier("93137ee3-580e-45f7-a61a-e0b5716000ef",
                ModifyCallAction.MUTE
        ));
        assertEquals("Received", call.getMessage());
    }

    @Test
    public void testTransferCall() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200, "{\"message\":\"Received\"}"));
        ModifyCallResponse call = client.transferCall("93137ee3-580e-45f7-a61a-e0b5716000ef",
                "https://example.com/ncco2"
        );
        assertEquals("Received", call.getMessage());
    }

    @Test
    public void testTransferCallInlineNcco() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200, "{\"message\":\"Received\"}"));
        ModifyCallResponse call = client.transferCall("93137ee3-580e-45f7-a61a-e0b5716000ef",
                new Ncco(TalkAction.builder("Thank you for calling!").build(), TalkAction.builder("Bye!").build())
        );
        assertEquals("Received", call.getMessage());
    }

    @Test
    public void testStartStreamNonLooping() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Stream started\",\n"
                        + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n" + "}"
        ));
        StreamResponse response = client.startStream(
                "ssf61863-4a51-ef6b-11e1-w6edebcf93bb",
                "https://nexmo-community.github.io/ncco-examples/assets/voice_api_audio_streaming.mp3"
        );
        assertEquals("Stream started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStartStreamLooping() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Stream started\",\n"
                        + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n" + "}"
        ));
        StreamResponse response = client.startStream(
                "ssf61863-4a51-ef6b-11e1-w6edebcf93bb",
                "https://nexmo-community.github.io/ncco-examples/assets/voice_api_audio_streaming.mp3",
                5
        );
        assertEquals("Stream started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStopStream() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Stream stopped\",\n"
                        + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n" + "}\n"
        ));

        StreamResponse response = client.stopStream("ssf61863-4a51-ef6b-11e1-w6edebcf93bb");
        assertEquals("Stream stopped", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStartTalkAllParams() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Talk started\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}\n"
        ));

        TalkResponse response = client.startTalk("ssf61863-4a51-ef6b-11e1-w6edebcf93bb",
                "Hello World",
                VoiceName.CELINE,
                8
        );
        assertEquals("Talk started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStartTalkNonLooping() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Talk started\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}\n"
        ));

        TalkResponse response = client.startTalk("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", "Hello World", VoiceName.EMMA);
        assertEquals("Talk started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStartTalkLoopingWithDefaultVoice() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Talk started\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}\n"
        ));

        TalkResponse response = client.startTalk("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", "Hello World", 3);
        assertEquals("Talk started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStartTalkNonLoopingWithDefaultVoice() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Talk started\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}\n"
        ));

        TalkResponse response = client.startTalk("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", "Hello World");
        assertEquals("Talk started", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }

    @Test
    public void testStopTalk() throws Exception {
        VoiceClient client = new VoiceClient(stubHttpWrapper(200,
                "{\n" + "  \"message\": \"Talk stopped\",\n" + "  \"uuid\": \"ssf61863-4a51-ef6b-11e1-w6edebcf93bb\"\n"
                        + "}\n"
        ));

        TalkResponse response = client.stopTalk("ssf61863-4a51-ef6b-11e1-w6edebcf93bb");
        assertEquals("Talk stopped", response.getMessage());
        assertEquals("ssf61863-4a51-ef6b-11e1-w6edebcf93bb", response.getUuid());
    }
}
