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

    function valueOrDash(value) {
        return value && String(value).trim() ? value : '—';
    }

    function fillEdit(service) {
        $('#edit-service-id').val(service.serviceId);
        $('#edit-service-name').val(service.serviceNombre);
        $('#edit-service-description').val(service.serviceDescripcion || '');
        $('#edit-service-price').val(service.servicePrecio);
        $('#edit-service-active').prop('checked', String(service.serviceActivo) === 'true');
        $('#edit-service-subtitle').text(`Actualiza los datos de ${valueOrDash(service.serviceNombre)}.`);
    }

    function fillDelete(service) {
        $('#delete-service-id').val(service.serviceId);
        $('#delete-service-name').text(`“${valueOrDash(service.serviceNombre)}”`);
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

        $(document).on('click', '.js-service-action', function (event) {
            event.preventDefault();
            event.stopPropagation();
            if ($(this).prop('disabled')) {
                return;
            }
            const service = $(this).closest('.service-record').data();
            const action = $(this).data('serviceAction');
            if (action === 'edit') {
                fillEdit(service);
                showModal('#modal-edit-service');
            } else if (action === 'delete') {
                fillDelete(service);
                showModal('#modal-delete-service');
            }
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

        $('#new-service-form, #edit-service-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-service-form').on('submit', function (event) {
            if (!window.Swal || $(this).data('confirmed')) {
                return;
            }
            event.preventDefault();
            const form = this;
            Swal.fire({
                icon: 'warning',
                title: '¿Desactivar este servicio?',
                text: 'El servicio dejará de estar disponible para nuevas cotizaciones.',
                showCancelButton: true,
                confirmButtonText: 'Sí, desactivar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#dc2626'
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
                timer: 3200,
                showConfirmButton: false
            });
        }

        const openModal = $page.attr('data-open-modal');
        if (openModal === 'nuevo') {
            showModal('#modal-new-service');
        } else if (openModal === 'editar') {
            showModal('#modal-edit-service');
        }
    });
})(jQuery);