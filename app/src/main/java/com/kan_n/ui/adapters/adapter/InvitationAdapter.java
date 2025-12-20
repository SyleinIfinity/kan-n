package com.kan_n.ui.adapters.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kan_n.R;
import com.kan_n.data.models.Invitation;
import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder> {

    private List<Invitation> invitations;
    private final OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onAccept(Invitation invitation);
        void onDecline(Invitation invitation);
    }

    public InvitationAdapter(List<Invitation> invitations, OnInvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    public void setData(List<Invitation> list) {
        this.invitations = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);

        String content = invitation.getSenderName() + " mời bạn tham gia bảng \"" + invitation.getBoardName() + "\"";
        holder.tvContent.setText(content);

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(invitation));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(invitation));
    }

    @Override
    public int getItemCount() {
        return invitations != null ? invitations.size() : 0;
    }

    public static class InvitationViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        Button btnAccept, btnDecline;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_invite_content);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }
}