package com.example.qurandaily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Drawer list for chapters/surahs.
 */
class ChapterAdapter(
    private var chapters: MutableList<ChapterUiModel>,
    private val onChapterClick: (ChapterUiModel) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    fun updateList(newList: List<ChapterUiModel>) {
        chapters.clear()
        chapters.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.bind(chapter, onChapterClick)
    }

    override fun getItemCount(): Int = chapters.size

    class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNumber: TextView = itemView.findViewById(R.id.tv_chapter_number)
        private val tvNameEn: TextView = itemView.findViewById(R.id.tv_chapter_name_en)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_chapter_meta)
        private val tvNameAr: TextView = itemView.findViewById(R.id.tv_chapter_name_ar)

        fun bind(chapter: ChapterUiModel, onChapterClick: (ChapterUiModel) -> Unit) {
            tvNumber.text = chapter.number.toString()
            tvNameEn.text = chapter.englishName
            tvMeta.text = chapter.meta
            tvNameAr.text = chapter.arabicName

            itemView.setOnClickListener { onChapterClick(chapter) }
        }
    }
}
