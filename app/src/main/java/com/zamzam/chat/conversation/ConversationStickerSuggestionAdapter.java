package com.zamzam.chat.conversation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.zamzam.chat.R;
import com.zamzam.chat.database.model.StickerRecord;
import com.zamzam.chat.mms.DecryptableStreamUriLoader.DecryptableUri;
import com.zamzam.chat.mms.GlideRequests;

import java.util.ArrayList;
import java.util.List;

public class ConversationStickerSuggestionAdapter extends RecyclerView.Adapter<ConversationStickerSuggestionAdapter.StickerSuggestionViewHolder> {

  private final GlideRequests       glideRequests;
  private final EventListener       eventListener;
  private final List<StickerRecord> stickers;

  public ConversationStickerSuggestionAdapter(@NonNull GlideRequests glideRequests, @NonNull EventListener eventListener) {
    this.glideRequests = glideRequests;
    this.eventListener = eventListener;
    this.stickers      = new ArrayList<>();
  }

  @Override
  public @NonNull StickerSuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    return new StickerSuggestionViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sticker_suggestion_list_item, viewGroup, false));
  }

  @Override
  public void onBindViewHolder(@NonNull StickerSuggestionViewHolder viewHolder, int i) {
    viewHolder.bind(glideRequests, eventListener, stickers.get(i));
  }

  @Override
  public void onViewRecycled(@NonNull StickerSuggestionViewHolder holder) {
    holder.recycle();
  }

  @Override
  public int getItemCount() {
    return stickers.size();
  }

  public void setStickers(@NonNull List<StickerRecord> stickers) {
    this.stickers.clear();
    this.stickers.addAll(stickers);
    notifyDataSetChanged();
  }

  static class StickerSuggestionViewHolder extends RecyclerView.ViewHolder {

    private final ImageView image;

    StickerSuggestionViewHolder(@NonNull View itemView) {
      super(itemView);
      this.image = itemView.findViewById(R.id.sticker_suggestion_item_image);
    }

    void bind(@NonNull GlideRequests glideRequests, @NonNull EventListener eventListener, @NonNull StickerRecord sticker) {
      glideRequests.load(new DecryptableUri(sticker.getUri()))
                   .transition(DrawableTransitionOptions.withCrossFade())
                   .into(image);

      itemView.setOnClickListener(v -> {
        eventListener.onStickerSuggestionClicked(sticker);
      });
    }

    void recycle() {
      itemView.setOnClickListener(null);
    }
  }

  public interface EventListener {
    void onStickerSuggestionClicked(@NonNull StickerRecord sticker);
  }
}
