(function ($) {
    'use strict';

    function createIcons() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    function showModal(selector) {
        $(selector).removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        createIcons();
    }

    function closeModal($modal) {
        $modal.addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        if (!$('.service-modal.flex').length) {
            $('body').removeClass('overflow-hidden');
        }
    }

    function validateForm(form) {
        const $form = $(form);
        $form.find('.service-form-control').removeClass('border-destructive ring-1 ring-destructive');

        if (form.checkValidity()) {
            return true;
        }

        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) {
            $(firstInvalid).addClass('border-destructive ring-1 ring-destructive').trigger('focus');
        }
        if (window.Swal) {
            Swal.fire({
                icon: 'warning',
                title: 'Revisa el formulario',
                text: 'Completa correctamente los campos obligatorios antes de continuar.',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#45d4b4'
            });
        }
        return false;
    }

    $(function () {
        createIcons();
        $('.service-modal').attr('aria-hidden', 'true');

        $('#btn-new-service').on('click', function () {
            showModal('#modal-new-service');
        });

        $('.js-service-close-modal').on('click', function () {
            closeModal($(this).closest('.service-modal'));
        });

        $('.service-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                $('.service-modal').each(function () {
                    closeModal($(this));
                });
            }
        });

        $('#new-service-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        const $page = $('#page-content');
        const successMessage = $page.attr('data-success-message');
        if (successMessage && window.Swal) {
            Swal.fire({
                icon: 'success',
                title: 'Operación completada',
                text: successMessage,
                timer: 3200,
                showConfirmButton: false
            });
        }

        if ($page.attr('data-open-modal') === 'nuevo') {
            showModal('#modal-new-service');
        }
    });
})(jQuery);