package com.mr0kaushik.newsapplication.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.data.Article
import kotlinx.android.synthetic.main.item_headline.view.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    private var onItemClickListener: ((Article) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_headline, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this)
                .load(article.urlToImage)
                .placeholder(R.drawable.logo)
                .fitCenter()
                .into(ivItemPicture)

            tvItemSource.text = article.source.name
            tvItemTitle.text = article.title
            tvItemAuthorAndDate.text = addBulletString(article.author ?: "", article.publishedAt)
            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    private fun addBulletString(author: String, date: String): String {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
            val zdt: ZonedDateTime = ZonedDateTime.parse(date, formatter)
            val prettyTime = DateTimeFormatter.ofPattern("dd MMM yyyy").format(zdt)
            if (author.isNotEmpty()) {
                "$author \u25CF $prettyTime"
            } else {
                return prettyTime
            }
        } else {
            author
        }
    }


    fun setOnItemClickListener(listener: ((Article) -> Unit)) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}