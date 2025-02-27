document.addEventListener('DOMContentLoaded', function() {
    const likeForms = document.querySelectorAll('.like-form');

    likeForms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            const postId = this.action.match(/\/likes\/([^/]+)\/toggle/)[1];
            const likeBtn = this.querySelector('.like-btn');
            const heart = likeBtn.querySelector('.heart');
            const count = likeBtn.querySelector('.count');
            const likeText = likeBtn.querySelector('.like-text');

            try {
                const response = await fetch(`/likes/${postId}/toggle`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                if (response.ok) {
                    const data = await response.json();

                    heart.textContent = data.liked ? '❤' : '♡';
                    heart.style.color = data.liked ? 'red' : 'black';
                    count.textContent = data.likeCount;

                    if (likeText) {
                        likeText.textContent = data.liked ? 'Liked' : 'Like';
                    }

                    if (data.liked) {
                        likeBtn.classList.add('already-liked');
                    } else {
                        likeBtn.classList.remove('already-liked');
                    }

                    // Update data attribute
                    likeBtn.setAttribute('data-liked', data.liked);
                }
            } catch (error) {
                console.error('Error toggling like:', error);
            }
        });
    });

    function refreshLikeCounts() {
        document.querySelectorAll('.post-card').forEach(async (postCard) => {
            try {
                // Extract post ID from the like form
                const likeForm = postCard.querySelector('.like-form');
                if (!likeForm) return;

                const postId = likeForm.action.match(/\/likes\/([^/]+)\/toggle/)[1];

                const response = await fetch(`/likes/${postId}/status`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (response.ok) {
                    const data = await response.json();

                    // Update the like count
                    const countElement = postCard.querySelector('.count');
                    if (countElement) {
                        countElement.textContent = data.likeCount;
                    }
                }
            } catch (error) {
                console.error('Error refreshing like count:', error);
            }
        });
    }

    setInterval(refreshLikeCounts, 10000);

    function updateNotificationCount() {
        fetch('/notifications/unread-count')
            .then(response => response.json())
            .then(count => {
                const badge = document.querySelector('.notification-badge');
                if (count > 0) {
                    if (!badge) {
                        const notificationLink = document.querySelector('.notification-link');
                        const newBadge = document.createElement('span');
                        newBadge.className = 'notification-badge';
                        newBadge.textContent = count;
                        notificationLink.appendChild(newBadge);
                    } else {
                        badge.textContent = count;
                    }
                } else if (badge) {
                    badge.remove();
                }
            })
            .catch(error => {
                console.error('Error updating notification count:', error);
            });
    }

    setInterval(updateNotificationCount, 30000);
    updateNotificationCount();
});