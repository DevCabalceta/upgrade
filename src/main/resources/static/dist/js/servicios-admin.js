(function ($) {
    'use strict';

    const VIEW_KEY = 'upgrade-services-view';
    let searchTimer;

    function createIcons() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    function openModal(selector) {
        $('.service-admin-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $(selector).removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        createIcons();
    }

    function closeModal($modal) {
        $modal.addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $('body').removeClass('overflow-hidden');
        $('.service-form-control, #delete-service-confirmation').removeClass('border-destructive ring-1 ring-destructive');
    }

    function closeAllModals() {
        $('.service-admin-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $('body').removeClass('overflow-hidden');
    }

    function serviceFromRecord($record) {
        return $record.data();
    }

    function asBoolean(value) {
        return value === true || String(value) === 'true';
    }

    function fillEdit(service) {
        $('#edit-service-id').val(service.serviceId);
        $('#edit-service-name').val(service.serviceName);
        $('#edit-service-description').val(service.serviceDescription);
        $('#edit-service-price').val(service.servicePrice);
        $('#edit-service-category').val(String(service.serviceCategoryId));
        $('#edit-service-unit').val(service.serviceUnit);
        $('#edit-service-duration').val(service.serviceDuration === 'No especificada' ? '' : service.serviceDuration);
        $('#edit-service-equipment').val(service.serviceEquipment);
        $('#edit-service-status').val(service.serviceStatus);
        $('#edit-service-icon').val(service.serviceIcon);
        $('#edit-service-order').val(service.serviceOrder);
        $('#edit-service-visible').prop('checked', asBoolean(service.serviceVisible));
        updateCounter($('#edit-service-form'));
    }

    function fillDetails(service) {
        $('#detail-service-name').text(service.serviceName);
        $('#detail-service-category').text(`${service.serviceCategory} · Posición ${service.serviceOrder}`);
        $('#detail-service-description').text(service.serviceDescription);
        $('#detail-service-price').text(service.servicePriceFormatted);
        $('#detail-service-unit').text(service.serviceUnitLabel);
        $('#detail-service-equipment').text(service.serviceEquipment);
        $('#detail-service-ytd').text(service.serviceYtd);
        $('#detail-service-duration').text(service.serviceDuration || 'No especificada');

        const $status = $('#detail-service-status');
        $status.text(service.serviceStatusLabel)
            .removeClass('border-emerald-500/30 bg-emerald-50 text-emerald-700 border-amber-500/30 bg-amber-50 text-amber-700 border-border bg-muted text-muted-foreground');
        if (service.serviceStatus === 'ACTIVO') {
            $status.addClass('border-emerald-500/30 bg-emerald-50 text-emerald-700');
        } else if (service.serviceStatus === 'BORRADOR') {
            $status.addClass('border-amber-500/30 bg-amber-50 text-amber-700');
        } else {
            $status.addClass('border-border bg-muted text-muted-foreground');
        }

        const published = service.serviceStatus === 'ACTIVO' && asBoolean(service.serviceVisible);
        $('#detail-service-published')
            .text(published ? 'Publicado en landing' : 'No publicado')
            .toggleClass('border-sky-500/30 bg-sky-500/10 text-sky-500', published)
            .toggleClass('border-border bg-muted text-muted-foreground', !published);
        $('#detail-service-icon').attr('data-lucide', service.serviceIcon || 'sparkles');
        createIcons();
    }

    function fillDelete(service) {
        $('#delete-service-id').val(service.serviceId);
        $('#delete-service-name').text(`“${service.serviceName}”`);
        $('#delete-service-confirmation').val('');
    }

    function updateCounter($form) {
        const value = $form.find('.service-description-field').val() || '';
        $form.find('.service-description-count').text(`${value.length}/1000`);
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
                text: 'Completa correctamente los campos obligatorios.',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#45d4b4'
            });
        }
        return false;
    }

    function changeView(view) {
        const cards = view === 'cards';
        $('#service-view-table').toggleClass('hidden', cards);
        $('#service-view-cards').toggleClass('hidden', !cards).toggleClass('grid', cards);
        $('.js-service-view-tab').removeClass('bg-background text-foreground shadow');
        $(`.js-service-view-tab[data-view="${view}"]`).addClass('bg-background text-foreground shadow');
        localStorage.setItem(VIEW_KEY, view);
        createIcons();
    }

    $(function () {
        createIcons();
        $('.service-admin-modal').attr('aria-hidden', 'true');

        $('#btn-new-service').on('click', function () {
            const form = $('#new-service-form')[0];
            form.reset();
            $('#new-service-visible').prop('checked', true);
            updateCounter($('#new-service-form'));
            openModal('#modal-new-service');
        });

        $('#btn-toggle-service-filters').on('click', function () {
            $('#service-filters').toggleClass('hidden').toggleClass('grid');
        });

        $('#service-search').on('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                $('#service-filter-form').trigger('submit');
            }, 450);
        });

        $('#service-status-filter, #service-category-filter').on('change', function () {
            $('#service-filter-form').trigger('submit');
        });

        $('.js-service-view-tab').on('click', function () {
            changeView($(this).data('view'));
        });

        $(document).on('click', '.js-service-menu-toggle', function (event) {
            event.stopPropagation();
            const $menu = $(this).siblings('.service-card-menu');
            $('.service-card-menu').not($menu).addClass('hidden');
            $menu.toggleClass('hidden');
        });

        $(document).on('click', function (event) {
            if (!$(event.target).closest('.service-card-menu, .js-service-menu-toggle').length) {
                $('.service-card-menu').addClass('hidden');
            }
        });

        $(document).on('click', '.js-service-action', function (event) {
            event.preventDefault();
            event.stopPropagation();
            const service = serviceFromRecord($(this).closest('.service-record'));
            const action = $(this).data('serviceAction');
            $('.service-card-menu').addClass('hidden');
            if (action === 'details') {
                fillDetails(service);
                openModal('#modal-service-details');
            } else if (action === 'edit') {
                fillEdit(service);
                openModal('#modal-edit-service');
            } else if (action === 'delete') {
                fillDelete(service);
                openModal('#modal-delete-service');
            }
        });

        $(document).on('input', '.service-description-field', function () {
            updateCounter($(this).closest('form'));
        });

        $('.js-service-close-modal').on('click', function () {
            closeModal($(this).closest('.service-admin-modal'));
        });

        $('.service-admin-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAllModals();
            }
        });

        $('#new-service-form, #edit-service-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-service-form').on('submit', function (event) {
            if ($('#delete-service-confirmation').val().trim() !== 'ELIMINAR') {
                event.preventDefault();
                $('#delete-service-confirmation').addClass('border-destructive ring-1 ring-destructive').trigger('focus');
                if (window.Swal) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Confirmación incorrecta',
                        text: 'Escribe ELIMINAR exactamente como se muestra.',
                        confirmButtonColor: '#45d4b4'
                    });
                }
            }
        });

        $('.js-service-publish-form').on('submit', function (event) {
            if (!window.Swal || $(this).data('confirmed')) {
                return;
            }
            event.preventDefault();
            const form = this;
            const visible = asBoolean($(form).closest('.service-record').data('serviceVisible'));
            Swal.fire({
                icon: 'question',
                title: visible ? '¿Ocultar servicio?' : '¿Habilitar publicación?',
                text: visible ? 'Dejará de mostrarse en la landing.' : 'Se mostrará cuando el estado del servicio sea Activo.',
                showCancelButton: true,
                confirmButtonText: visible ? 'Sí, ocultar' : 'Sí, habilitar',
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
        const errorMessage = $page.attr('data-error-message');
        if (successMessage && window.Swal) {
            Swal.fire({icon: 'success', title: 'Operación completada', text: successMessage, timer: 3000, showConfirmButton: false});
        } else if (errorMessage && window.Swal) {
            Swal.fire({icon: 'error', title: 'No se completó la operación', text: errorMessage, confirmButtonColor: '#45d4b4'});
        }

        updateCounter($('#new-service-form'));
        updateCounter($('#edit-service-form'));
        const requestedView = localStorage.getItem(VIEW_KEY) === 'cards' ? 'cards' : 'table';
        changeView(requestedView);

        const openModalName = $page.attr('data-open-modal');
        if (openModalName === 'nuevo') {
            openModal('#modal-new-service');
        } else if (openModalName === 'editar') {
            openModal('#modal-edit-service');
        }
    });
})(jQuery);
