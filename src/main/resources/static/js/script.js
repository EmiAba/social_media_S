document.addEventListener('DOMContentLoaded', function() {
    function refreshLikeCounts() {
        document.querySelectorAll('.post-card').forEach(async (postCard) => {
            try {
                const likeForm = postCard.querySelector('form[action*="/likes/"]');
                if (!likeForm) return;
                const matches = likeForm.action.match(/\/likes\/([^/]+)\/toggle/);
if (!matches || matches.length < 2) return;

const postId = matches[1];

const response = await fetch(`/likes/${postId}/status`, {
    method: 'GET',
    credentials: 'include'
});

if (response.ok) {
    const data = await response.json();

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

setInterval(refreshLikeCounts, 30000);

function updateNotificationCount() {
    fetch('/notifications/unread-count')
        .then(response => response.json())
        .then(count => {
            const badge = document.querySelector('.notification-badge');
            if (count > 0) {
                if (!badge) {
                    const notificationLink = document.querySelector('a[href="/notifications"]');
                    if (notificationLink) {
                        const newBadge = document.createElement('span');
                        newBadge.className = 'notification-badge';
                        newBadge.textContent = count;
                        notificationLink.appendChild(newBadge);
                    }
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

updateNotificationCount();
setInterval(updateNotificationCount, 30000);

function sendHeartbeat() {
    fetch('/api/heartbeat', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    }).catch(error => {
        console.error('Error sending heartbeat:', error);
    });
}

sendHeartbeat();
setInterval(sendHeartbeat, 60000);
});