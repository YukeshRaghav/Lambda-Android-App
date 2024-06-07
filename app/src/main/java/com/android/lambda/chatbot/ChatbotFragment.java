package com.android.lambda.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.lambda.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotFragment extends Fragment {
    private OkHttpClient client;
    private ArrayList<Message> dialog;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private Button sendButton;
    private Button promptSendButton;
    private EditText message;
    private Spinner promptMessage;
    private Map<String, String> promptDescriptions = new HashMap<>();
    private String currentPrompt = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chatbot_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.message_box);
        sendButton = view.findViewById(R.id.send_button);
        message = view.findViewById(R.id.input_message);
        promptMessage = view.findViewById(R.id.prompt_message);
        promptSendButton = view.findViewById(R.id.prompt_send_button);
        client = new OkHttpClient();
        dialog = new ArrayList<>();
        messageAdapter = new MessageAdapter(dialog);

        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        populatePromptDescriptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(promptDescriptions.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promptMessage.setAdapter(adapter);

        promptSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String role = promptMessage.getSelectedItem().toString();
                String rolePrompt = promptDescriptions.get(role);

                addToChat(rolePrompt, Message.SENT_BY_ME);

                currentPrompt = rolePrompt;
            }
        });


        sendButton.setOnClickListener(v -> {
            String userMessage = message.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addToChat(userMessage, Message.SENT_BY_ME);
                String combinedMessage = currentPrompt.isEmpty() ? userMessage : currentPrompt + "\n" + userMessage;
                message.setText("");
                callAPI(combinedMessage);
            }
        });
    }

    private void populatePromptDescriptions() {
        promptDescriptions.put("Market Analyst", "You will be provided with a product name and price. Your task is to mention if the price is high or less based on your knowledge. And finally, give the usual range for the item.");
        promptDescriptions.put("Coder", "You will be provided with a piece of code, and your task is to explain it in a concise way.");
        promptDescriptions.put("Math Teacher", "I want you to act as a math teacher...");
        promptDescriptions.put("Friend", "I want you to act as my friend...");
        promptDescriptions.put("Debater", "I want you to act as a debater...");
    }

    public void callAPI(String prompt) {
        JSONObject requestBody = createRequestJSON(prompt);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString());
        String apiKey = getString(R.string.API_KEY);
        String authorizationHeader = "Bearer " + apiKey;

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", authorizationHeader)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                addToChat("Failed to load response due to " + e.getMessage(), Message.SENT_BY_BOT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addToChat(result, Message.SENT_BY_BOT);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    addToChat("Empty response received", Message.SENT_BY_BOT);
                }
            }
        });
    }

    private JSONObject createRequestJSON(String prompt) {
        JSONObject requestBody = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject msg = new JSONObject();
        try {
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0);
            msg.put("role", "user");
            msg.put("content", prompt);
            messages.put(msg);
            requestBody.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    void addToChat(String message, String sentBy) {
        requireActivity().runOnUiThread(() -> {
            dialog.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }
}