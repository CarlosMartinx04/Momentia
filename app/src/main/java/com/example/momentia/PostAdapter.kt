package com.example.momentia

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserPhoto: ImageView = itemView.findViewById(R.id.ivUserPhoto)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val currentUid = auth.currentUser?.uid ?: ""

        holder.tvUserName.text = post.userName
        holder.tvLocation.text = post.location
        holder.tvDescription.text = post.description
        holder.tvDate.text =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(post.createdAt))

        if (post.userPhoto.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(post.userPhoto)
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.ivUserPhoto)
        }

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .centerCrop()
            .placeholder(android.R.color.darker_gray)
            .into(holder.ivPostImage)

        val hasLiked = post.likes.contains(currentUid)

        updateLikeButton(holder.btnLike, hasLiked)
        holder.tvLikeCount.text = post.likes.size.toString()

        holder.btnLike.setOnClickListener {
            toggleLike(post, holder, position)
        }

        holder.ivPostImage.setOnClickListener {
            val open = holder.itemView.context
            open.startActivity(Intent(open, PostDetailActivity::class.java).apply {
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

    override fun getItemCount() = posts.size

    private fun updateLikeButton(btn: ImageButton, hasLiked: Boolean) {
        btn.setImageResource(
            if (hasLiked)
                R.drawable.heart
            else
                R.drawable.heart_inactive
        )
    }

    private fun toggleLike(post: Post, holder: PostViewHolder, position: Int) {
        val currentUid = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(post.id)

        db.runTransaction { transaction ->
            val likes = transaction.get(postRef)
                .get("likes") as? MutableList<String> ?: mutableListOf()

            if (likes.contains(currentUid)) {
                likes.remove(currentUid)
            } else {
                likes.add(currentUid)
                if (post.uid != currentUid) {
                    createLikeNotification(post, currentUid)
                }
            }

            transaction.update(postRef, "likes", likes)
            likes
        }.addOnSuccessListener { updatedLikes ->

            posts[position] = post.copy(likes = updatedLikes)

            holder.tvLikeCount.text = updatedLikes.size.toString()

            updateLikeButton(
                holder.btnLike,
                updatedLikes.contains(currentUid)
            )
        }
    }

    private fun createLikeNotification(post: Post, fromUid: String) {
        db.collection("users").document(fromUid).get()
            .addOnSuccessListener { userDoc ->
                val fromName = userDoc.getString("name") ?: "Alguien"

                db.collection("notifications").add(
                    hashMapOf(
                        "toUid" to post.uid,
                        "fromUid" to fromUid,
                        "fromName" to fromName,
                        "type" to "like",
                        "postId" to post.id,
                        "postImage" to post.imageUrl,
                        "message" to "$fromName le dio like a tu foto",
                        "read" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            }
    }
}