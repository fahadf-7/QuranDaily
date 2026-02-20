package com.example.qurandaily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for ayah cards:
 * - shows Arabic
 * - toggles translation panel
 * - handles bookmark + play clicks
 */
class AyahAdapter(
    private var items: List<AyahUiModel> = emptyList(),
    private val onBookmarkClick: (AyahUiModel) -> Unit,
    private val onTafseerClick: (AyahUiModel) -> Unit,
    private val onPlayClick: (AyahUiModel) -> Unit
) : RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    // which ayahs currently have their translation shown
    private val expandedAyahs = mutableSetOf<String>()  // verseKey, e.g. "2:255"

    fun submitList(newItems: List<AyahUiModel>) {
        items = newItems
        expandedAyahs.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        val item = items[position]
        val isExpanded = expandedAyahs.contains(item.indexLabel)
        holder.bind(
            ayah = item,
            expanded = isExpanded,
            onBookmarkClick = onBookmarkClick,
            onToggleTranslation = { ayah, nowExpanded ->
                if (nowExpanded) expandedAyahs.add(ayah.indexLabel)
                else expandedAyahs.remove(ayah.indexLabel)
                onTafseerClick(ayah)
            },
            onPlayClick = onPlayClick
        )
    }

    override fun getItemCount(): Int = items.size

    class AyahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvAyahNumber: TextView = itemView.findViewById(R.id.tv_ayah_number)
        private val tvAyahIndex: TextView = itemView.findViewById(R.id.tv_ayah_index)
        private val tvAyahArabic: TextView = itemView.findViewById(R.id.tv_ayah_arabic)
        private val tvAyahTranslation: TextView = itemView.findViewById(R.id.tv_ayah_translation)

        private val btnBookmark: ImageButton = itemView.findViewById(R.id.btn_ayah_bookmark)
        private val btnShowTafseer: Button = itemView.findViewById(R.id.btn_show_tafseer)
        private val btnPlay: ImageButton = itemView.findViewById(R.id.btn_ayah_play)

        private val layoutTranslation: LinearLayout =
            itemView.findViewById(R.id.layout_translation_modal)

        fun bind(
            ayah: AyahUiModel,
            expanded: Boolean,
            onBookmarkClick: (AyahUiModel) -> Unit,
            onToggleTranslation: (AyahUiModel, Boolean) -> Unit,
            onPlayClick: (AyahUiModel) -> Unit
        ) {
            tvAyahNumber.text = ayah.ayahNumber.toString()
            tvAyahIndex.text = ayah.indexLabel
            tvAyahArabic.text = ayah.arabicText
            tvAyahTranslation.text = ayah.translationText

            // current state
            layoutTranslation.visibility = if (expanded) View.VISIBLE else View.GONE
            btnShowTafseer.text =
                if (expanded) "Hide Translation (English)" else "Show Translation (English)"

            btnBookmark.setOnClickListener { onBookmarkClick(ayah) }

            btnShowTafseer.setOnClickListener {
                val nowExpanded = layoutTranslation.visibility != View.VISIBLE
                layoutTranslation.visibility =
                    if (nowExpanded) View.VISIBLE else View.GONE
                btnShowTafseer.text =
                    if (nowExpanded) "Hide Translation (English)" else "Show Translation (English)"
                onToggleTranslation(ayah, nowExpanded)
            }

            btnPlay.setOnClickListener {
                onPlayClick(ayah)
            }
        }
    }
}
