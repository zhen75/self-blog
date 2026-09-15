(() => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const toastElement = document.querySelector('#admin-toast');
    const messages = document.querySelector('#admin-page')?.dataset || {};

    function showToast(message, success) {
        if (!toastElement) return;
        toastElement.classList.remove('text-bg-success', 'text-bg-danger');
        toastElement.classList.add(success ? 'text-bg-success' : 'text-bg-danger');
        toastElement.querySelector('.toast-body').textContent = message;
        bootstrap.Toast.getOrCreateInstance(toastElement, { delay: 2200 }).show();
    }

    async function request(url, method) {
        const headers = {};
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
        const response = await fetch(url, { method, headers });
        if (!response.ok) {
            const message = (await response.text()).trim();
            throw new Error(message || messages.requestFailed.replace('__STATUS__', response.status));
        }
    }

    document.addEventListener('click', async (event) => {
        const button = event.target.closest('.js-post-action');
        if (!button) return;
        const { action, postId, postTitle } = button.dataset;
        const labels = {
            publish: { question: messages.publishQuestion, pending: messages.publishing, success: messages.published, method: 'PUT' },
            withdraw: { question: messages.withdrawQuestion, pending: messages.withdrawing, success: messages.withdrawn, method: 'PUT' },
            delete: { question: messages.deleteQuestion, pending: messages.deleting, success: messages.deleted, method: 'DELETE' }
        };
        const config = labels[action];
        if (!config || !window.confirm(config.question.replace('__TITLE__', postTitle))) return;
        const originalText = button.textContent;
        button.disabled = true;
        button.textContent = config.pending;
        try {
            const suffix = action === 'delete' ? '' : `/${action}`;
            await request(`/api/admin/articles/${postId}${suffix}`, config.method);
            showToast(config.success, true);
            window.setTimeout(() => window.location.reload(), 500);
        } catch (error) {
            showToast(error.message, false);
            button.disabled = false;
            button.textContent = originalText;
        }
    });
})();
