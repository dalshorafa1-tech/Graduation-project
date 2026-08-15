package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;

public class InitiativesAdapter extends RecyclerView.Adapter<InitiativesAdapter.InitiativeViewHolder> {

    private List<InitiativeModel> initiativeList;
    private OnItemClickListener listener;

    // واجهة تفاعلية (Interface) لمعالجة الأحداث والنقرات قادمة من الأنشطة (Activities)
    public interface OnItemClickListener {
        void onItemClick(InitiativeModel initiative);
        void onTrackProgressClick(InitiativeModel initiative);
    }

    // الباني (Constructor) بـ معاملين وهو المعتمد حالياً في كافة الأنشطة لديك لتفادي أخطاء البناء
    public InitiativesAdapter(List<InitiativeModel> initiativeList, OnItemClickListener listener) {
        this.initiativeList = initiativeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InitiativeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ربط ملف التصميم المخصص الذي أرسلته (تأكد أن الملف مسمى item_initiative.xml في المجلد res/layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_initiative, parent, false);
        return new InitiativeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InitiativeViewHolder holder, int position) {
        InitiativeModel initiative = initiativeList.get(position);
        holder.bind(initiative, listener);
    }

    @Override
    public int getItemCount() {
        return initiativeList != null ? initiativeList.size() : 0;
    }

    // الـ ViewHolder المسؤول عن ربط وتحديث عناصر الـ XML بالبيانات الحية القادمة من Firebase
    static class InitiativeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvStatusText, tvTargetVolume, tvProgressPercentage;
        LinearProgressIndicator progressBarInitiative;
        MaterialButton btnTrackProgress;
        MaterialCardView badgeStatus;

        public InitiativeViewHolder(@NonNull View itemView) {
            super(itemView);
            // ربط المعرفات (IDs) المطابقة تماماً لملف الـ XML المخصص لبطاقة المبادرة
            tvTitle = itemView.findViewById(R.id.tv_initiative_title);
            tvLocation = itemView.findViewById(R.id.tv_initiative_location);
            tvStatusText = itemView.findViewById(R.id.tv_status_text);
            tvTargetVolume = itemView.findViewById(R.id.tv_target_volume);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            progressBarInitiative = itemView.findViewById(R.id.progress_bar_initiative);
            btnTrackProgress = itemView.findViewById(R.id.btn_track_progress);
            badgeStatus = itemView.findViewById(R.id.badge_status);
        }

        public void bind(final InitiativeModel initiative, final OnItemClickListener listener) {
            // تعيين النصوص الأساسية
            tvTitle.setText(initiative.getTitle());
            tvLocation.setText("📍 " + initiative.getLocation());
            tvStatusText.setText(initiative.getStatus());
            tvTargetVolume.setText("الهدف: " + initiative.getTargetLiters() + " لتر");

            // حساب نسبة التقدم ديناميكياً من الموديل وضخها في الـ ProgressBar والـ Text
            int progress = initiative.getProgressPercentage();
            progressBarInitiative.setProgress(progress);
            tvProgressPercentage.setText(progress + "% مكتمل");

            // 🎨 تحكم ذكي بالـ Badge برمجياً بناءً على الحالة المستلمة من الفايربيس
            if ("مكتملة".equals(initiative.getStatus())) {
                badgeStatus.setCardBackgroundColor(0xFFE2E8F0); // رمادي خفيف للمبادرات المنتهية
                tvStatusText.setTextColor(0xFF475569);
            } else {
                badgeStatus.setCardBackgroundColor(0xFFCCFBF1); // تركوازي للمبادرات النشطة (قيد التمويل)
                tvStatusText.setTextColor(0xFF0D9488);
            }

            // 1️⃣ معالجة النقر العام على الكارد بالكامل
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(initiative);
                }
            });

            // 2️⃣ معالجة النقر المباشر على زر "تتبع التقدم" المخصص في تصميمك لنقل الـ ID للخرائط
            if (btnTrackProgress != null) {
                btnTrackProgress.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTrackProgressClick(initiative);
                    }
                });
            }
        }
    }
}