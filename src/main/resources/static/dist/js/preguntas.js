(function ($) {
    'use strict';

    let searchTimer;

    function createIcons() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    function openModal(selector) {
        $('.faq-admin-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $(selector).removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        createIcons();
    }

    function closeModal($modal) {
        $modal.addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $('body').removeClass('overflow-hidden');
        $('.faq-form-control').removeClass('border-destructive ring-1 ring-destructive');
    }

    function closeAllModals() {
        $('.faq-admin-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $('body').removeClass('overflow-hidden');
    }

    function faqFromRecord($record) {
        return $record.data();
    }

    function fillPreview(faq) {
        $('#preview-faq-question').text(faq.faqPregunta);
        $('#preview-faq-answer').text(faq.faqRespuesta);
        $('#preview-faq-category').text(faq.faqCategoriaEtiqueta);
        $('#preview-faq-order').text(`Posición ${faq.faqOrden}`);
        $('#preview-faq-status').text(String(faq.faqActiva) === 'true'
            ? 'Esta pregunta está publicada en la landing.'
            : 'Esta pregunta se encuentra oculta.');
    }

    function fillEdit(faq) {
        $('#edit-faq-id').val(faq.faqId);
        $('#edit-faq-question').val(faq.faqPregunta);
        $('#edit-faq-answer').val(faq.faqRespuesta);
        $('#edit-faq-category').val(faq.faqCategoria);
        $('#edit-faq-active').prop('checked', String(faq.faqActiva) === 'true');
        updateCounters($('#edit-faq-form'));
    }

    function fillDelete(faq) {
        $('#delete-faq-id').val(faq.faqId);
        $('#delete-faq-question').text(`“${faq.faqPregunta}”`);
        $('#delete-faq-confirmation').val('');
    }

    function updateCounters($form) {
        const questionLength = $form.find('.faq-question-field').val().length;
        const answerLength = $form.find('.faq-answer-field').val().length;
        $form.find('.faq-question-count').text(`${questionLength}/160`);
        $form.find('.faq-answer-count').text(`${answerLength}/600`);
    }

    function validateForm(form) {
        const $form = $(form);
        $form.find('.faq-form-control').removeClass('border-destructive ring-1 ring-destructive');
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
                text: 'Completa correctamente los campos obligatorios.',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#45d4b4'
            });
        }
        return false;
    }

    $(function () {
        createIcons();
        $('.faq-admin-modal').attr('aria-hidden', 'true');

        $('#btn-new-faq').on('click', function () {
            const form = $('#new-faq-form')[0];
            form.reset();
            $('#new-faq-form input[type="checkbox"]').prop('checked', true);
            updateCounters($('#new-faq-form'));
            openModal('#modal-new-faq');
        });

        $('#btn-toggle-faq-filters').on('click', function () {
            $('#faq-filters').toggleClass('hidden').toggleClass('grid');
        });

        $('#faq-search').on('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                $('#faq-filter-form').trigger('submit');
            }, 450);
        });

        $('#faq-status-filter, #faq-category-filter').on('change', function () {
            $('#faq-filter-form').trigger('submit');
        });

        $(document).on('input', '.faq-question-field, .faq-answer-field', function () {
            updateCounters($(this).closest('form'));
        });

        $(document).on('click', '.js-faq-preview', function () {
            const faq = faqFromRecord($(this).closest('.faq-record'));
            fillPreview(faq);
            openModal('#modal-faq-preview');
        });

        $(document).on('click', '.js-faq-action', function (event) {
            event.preventDefault();
            event.stopPropagation();
            const faq = faqFromRecord($(this).closest('.faq-record'));
            const action = $(this).data('faqAction');

            if (action === 'preview') {
                fillPreview(faq);
                openModal('#modal-faq-preview');
            } else if (action === 'edit') {
                fillEdit(faq);
                openModal('#modal-edit-faq');
            } else if (action === 'delete') {
                fillDelete(faq);
                openModal('#modal-delete-faq');
            }
        });

        $('.js-faq-close-modal').on('click', function () {
            closeModal($(this).closest('.faq-admin-modal'));
        });

        $('.faq-admin-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAllModals();
            }
        });

        $('#new-faq-form, #edit-faq-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-faq-form').on('submit', function (event) {
            if ($(this).data('confirmed')) {
                return;
            }
            event.preventDefault();

            if ($('#delete-faq-confirmation').val().trim().toUpperCase() !== 'ELIMINAR') {
                $('#delete-faq-confirmation')
                    .addClass('border-destructive ring-1 ring-destructive')
                    .trigger('focus');
                if (window.Swal) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Confirmación incorrecta',
                        text: 'Escribe ELIMINAR para confirmar la acción.',
                        confirmButtonColor: '#45d4b4'
                    });
                }
                return;
            }

            const form = this;
            if (!window.Swal) {
                $(form).data('confirmed', true);
                form.submit();
                return;
            }
            Swal.fire({
                icon: 'warning',
                title: '¿Eliminar definitivamente?',
                text: 'La pregunta desaparecerá del sistema y de la landing.',
                showCancelButton: true,
                confirmButtonText: 'Sí, eliminar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#dc2626'
            }).then(function (result) {
                if (result.isConfirmed) {
                    $(form).data('confirmed', true);
                    form.submit();
                }
            });
        });

        $('.js-faq-toggle-form').on('submit', function (event) {
            if (!window.Swal || $(this).data('confirmed')) {
                return;
            }
            event.preventDefault();
            const form = this;
            const currentlyActive = String($(form).closest('.faq-record').data('faqActiva')) === 'true';
            Swal.fire({
                icon: 'question',
                title: currentlyActive ? '¿Ocultar pregunta?' : '¿Publicar pregunta?',
                text: currentlyActive
                    ? 'Dejará de mostrarse en la landing.'
                    : 'Se mostrará en la landing según su posición actual.',
                showCancelButton: true,
                confirmButtonText: currentlyActive ? 'Sí, ocultar' : 'Sí, publicar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#45d4b4'
            }).then(function (result) {
                if (result.isConfirmed) {
                    $(form).data('confirmed', true);
                    form.submit();
                }
            });
        });

        const $page = $('#page-content');
        const successMessage = $page.attr('data-success-message');
        if (successMessage && window.Swal) {
            Swal.fire({
                icon: 'success',
                title: 'Operación completada',
                text: successMessage,
                timer: 3000,
                showConfirmButton: false
            });
        }

        updateCounters($('#new-faq-form'));
        updateCounters($('#edit-faq-form'));
        const openModalName = $page.attr('data-open-modal');
        if (openModalName === 'nueva') {
            openModal('#modal-new-faq');
        } else if (openModalName === 'editar') {
            openModal('#modal-edit-faq');
        }
    });
})(jQuery);
