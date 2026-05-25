package com.example.momentia

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private val posts: MutableList<Post>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val loadedAds = mutableListOf<NativeAd>()
    private val AD_EVERY_N_POSTS = 5

    companion object {
        const val TYPE_POST = 0
        const val TYPE_AD   = 1
    }

    init {
        MobileAds.initialize(context) {}
        loadNativeAds(3)
    }


    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserPhoto:   ImageView  = itemView.findViewById(R.id.ivUserPhoto)
        val tvUserName:    TextView   = itemView.findViewById(R.id.tvUserName)
        val tvLocation:    TextView   = itemView.findViewById(R.id.tvLocation)
        val ivPostImage:   ImageView  = itemView.findViewById(R.id.ivPostImage)
        val tvDescription: TextView   = itemView.findViewById(R.id.tvDescription)
        val btnLike:       ImageButton = itemView.findViewById(R.id.btnLike)
        val tvLikeCount:   TextView   = itemView.findViewById(R.id.tvLikeCount)
        val tvDate:        TextView   = itemView.findViewById(R.id.tvDate)
    }

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nativeAdView:   NativeAdView = itemView.findViewById(R.id.nativeAdView)
        val adAppIcon:      ImageView    = itemView.findViewById(R.id.adAppIcon)
        val adHeadline:     TextView     = itemView.findViewById(R.id.adHeadline)
        val adMediaView:    MediaView    = itemView.findViewById(R.id.adMediaView)
        val adBody:         TextView     = itemView.findViewById(R.id.adBody)
        val adCallToAction: Button       = itemView.findViewById(R.id.adCallToAction)
    }


    override fun getItemViewType(position: Int): Int {
        return if (position != 0 && position % (AD_EVERY_N_POSTS + 1) == AD_EVERY_N_POSTS) {
            TYPE_AD
        } else {
            TYPE_POST
        }
    }

    override fun getItemCount(): Int {
        val adSlots = posts.size / AD_EVERY_N_POSTS
        return posts.size + adSlots
    }

    private fun getPostIndex(position: Int): Int {
        val adsBefore = position / (AD_EVERY_N_POSTS + 1)
        return position - adsBefore
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_AD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ad, parent, false)
            AdViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            PostViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_AD) {
            bindAd(holder as AdViewHolder)
        } else {
            val postIndex = getPostIndex(position)
            if (postIndex < posts.size) {
                bindPost(holder as PostViewHolder, postIndex)
            }
        }
    }


    private fun bindPost(holder: PostViewHolder, position: Int) {
        val post       = posts[position]
        val currentUid = auth.currentUser?.uid ?: ""

        holder.tvUserName.text    = post.userName
        holder.tvLocation.text    = post.location
        holder.tvDescription.text = post.description
        holder.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(post.createdAt))

        if (post.userPhoto.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(post.userPhoto).circleCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.ivUserPhoto)
        }

        Glide.with(holder.itemView.context)
            .load(post.imageUrl).centerCrop()
            .placeholder(android.R.color.darker_gray)
            .into(holder.ivPostImage)

        val hasLiked = post.likes.contains(currentUid)
        updateLikeButton(holder.btnLike, hasLiked)
        holder.tvLikeCount.text = post.likes.size.toString()

        holder.btnLike.setOnClickListener { toggleLike(post, holder, position) }

        holder.ivPostImage.setOnClickListener {
            val ctx = holder.itemView.context
            ctx.startActivity(Intent(ctx, PostDetailActivity::class.java).apply {
                putExtra("postId",      post.id)
                putExtra("imageUrl",    post.imageUrl)
                putExtra("userName",    post.userName)
                putExtra("userPhoto",   post.userPhoto)
                putExtra("description", post.description)
                putExtra("location",    post.location)
                putExtra("lat",         post.lat)
                putExtra("lng",         post.lng)
                putExtra("createdAt",   post.createdAt)
            })
        }
    }


    private fun bindAd(holder: AdViewHolder) {
        if (loadedAds.isEmpty()) return

        holder.itemView.visibility = View.VISIBLE
        val nativeAd = loadedAds.removeAt(0)

        nativeAd.headline?.let { holder.adHeadline.text = it }
        nativeAd.body?.let { holder.adBody.text = it }
        nativeAd.callToAction?.let { holder.adCallToAction.text = it }
        nativeAd.icon?.drawable?.let { holder.adAppIcon.setImageDrawable(it) }

        holder.nativeAdView.apply {
            headlineView     = holder.adHeadline
            bodyView         = holder.adBody
            callToActionView = holder.adCallToAction
            iconView         = holder.adAppIcon
            mediaView        = holder.adMediaView
            setNativeAd(nativeAd)
        }
    }



    private fun updateLikeButton(btn: ImageButton, hasLiked: Boolean) {
        btn.setImageResource(if (hasLiked) R.drawable.heart else R.drawable.heart_inactive)
    }

    private fun toggleLike(post: Post, holder: PostViewHolder, position: Int) {
        val currentUid = auth.currentUser?.uid ?: return
        val postRef    = db.collection("posts").document(post.id)

        db.runTransaction { transaction ->
            @Suppress("UNCHECKED_CAST")
            val likes = transaction.get(postRef).get("likes") as? MutableList<String> ?: mutableListOf()
            if (likes.contains(currentUid)) likes.remove(currentUid)
            else {
                likes.add(currentUid)
                if (post.uid != currentUid) createLikeNotification(post, currentUid)
            }
            transaction.update(postRef, "likes", likes)
            likes
        }.addOnSuccessListener { updatedLikes ->
            posts[position] = post.copy(likes = updatedLikes)
            holder.tvLikeCount.text = updatedLikes.size.toString()
            updateLikeButton(holder.btnLike, updatedLikes.contains(currentUid))
        }
    }

    private fun createLikeNotification(post: Post, fromUid: String) {
        db.collection("users").document(fromUid).get()
            .addOnSuccessListener { userDoc ->
                val fromName = userDoc.getString("name") ?: "Alguien"
                db.collection("notifications").add(hashMapOf(
                    "toUid"     to post.uid,
                    "fromUid"   to fromUid,
                    "fromName"  to fromName,
                    "type"      to "like",
                    "postId"    to post.id,
                    "postImage" to post.imageUrl,
                    "message"   to "$fromName le dio like a tu foto",
                    "read"      to false,
                    "createdAt" to System.currentTimeMillis()
                ))
            }
    }

    fun destroyAds() {
        loadedAds.forEach { it.destroy() }
        loadedAds.clear()
    }

    private fun loadNativeAds(count: Int) {
        val adUnitId = context.getString(R.string.admob_native_ad_unit_id)
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { loadedAds.add(it) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {}
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                    .build()
            )
            .build()
            .loadAds(com.google.android.gms.ads.AdRequest.Builder().build(), count)
    }
}