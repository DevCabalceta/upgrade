(function ($) {
    'use strict';

    let searchTimer;

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
        if (!$('.equipment-modal.flex').length) {
            $('body').removeClass('overflow-hidden');
        }
    }

    function closeAllModals() {
        $('.equipment-modal').each(function () {
            closeModal($(this));
        });
    }

    function equipmentFromRecord($record) {
        return $record.data();
    }

    function valueOrDash(value) {
        return value === null || value === undefined || String(value).trim() === '' ? '—' : String(value);
    }

    function formatMoney(value) {
        if (value === null || value === undefined || value === '') {
            return '—';
        }
        const number = Number(value);
        return Number.isFinite(number)
            ? '₡' + new Intl.NumberFormat('es-CR', {minimumFractionDigits: 2, maximumFractionDigits: 2}).format(number)
            : valueOrDash(value);
    }

    function setText(selector, value) {
        $(selector).text(valueOrDash(value));
    }

    function stateClasses(state) {
        const base = 'rounded-md border px-2 py-0.5 text-xs font-semibold';
        if (state === 'DISPONIBLE') {
            return base + ' border-emerald-500/30 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400';
        }
        if (state === 'EN_PRESTAMO') {
            return base + ' border-amber-500/30 bg-amber-50 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400';
        }
        if (state === 'EN_MANTENIMIENTO') {
            return base + ' border-red-500/30 bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400';
        }
        if (state === 'EN_CAMION') {
            return base + ' border-primary/30 bg-primary/10 text-primary';
        }
        return base + ' border-border bg-muted text-muted-foreground';
    }

    function fillDetails(item) {
        setText('#detail-equipment-code', item.equipmentCode);
        setText('#detail-equipment-qr', 'QR-' + valueOrDash(item.equipmentCode));
        setText('#detail-equipment-name', item.equipmentName);
        setText('#detail-equipment-description', item.equipmentDescription || 'Sin descripción registrada.');
        setText('#detail-equipment-marca', item.equipmentBrand);
        setText('#detail-equipment-modelo', item.equipmentModel);
        setText('#detail-equipment-serie', item.equipmentSerial);
        setText('#detail-equipment-categoría', item.equipmentCategory);
        setText('#detail-equipment-stock-total', item.equipmentTotal);
        setText('#detail-equipment-disponible', item.equipmentAvailable);
        setText('#detail-equipment-bodega', item.equipmentWarehouse);
        setText('#detail-equipment-ubicación', item.equipmentPhysicalLocation);
        setText('#detail-equipment-costo-unitario', formatMoney(item.equipmentValue));
        setText('#detail-equipment-precio-venta', formatMoney(item.equipmentPrice));
        setText('#detail-equipment-adquisición', item.equipmentDate);
        $('#detail-equipment-state')
            .attr('class', stateClasses(item.equipmentState))
            .text(valueOrDash(item.equipmentStateLabel));
    }

    function fillEdit(item) {
        $('#edit-equipment-id').val(item.equipmentId);
        $('#edit-equipment-name').val(item.equipmentName || '');
        $('#edit-equipment-code').val(item.equipmentCode || '');
        $('#edit-equipment-category').val(item.equipmentCategoryId || '');
        $('#edit-equipment-serial').val(item.equipmentSerial || '');
        $('#edit-equipment-brand').val(item.equipmentBrand || '');
        $('#edit-equipment-model').val(item.equipmentModel || '');
        $('#edit-equipment-total').val(item.equipmentTotal);
        $('#edit-equipment-available').val(item.equipmentAvailable);
        $('#edit-equipment-state').val(item.equipmentState);
        $('#edit-equipment-warehouse').val(item.equipmentWarehouse || '');
        $('#edit-equipment-location').val(item.equipmentPhysicalLocation || '');
        $('#edit-equipment-date').val(item.equipmentDate || '');
        $('#edit-equipment-value').val(item.equipmentValue || 0);
        $('#edit-equipment-price').val(item.equipmentPrice || '');
        $('#edit-equipment-description').val(item.equipmentDescription || '');
        $('#edit-equipment-subtitle').text('Actualiza la información de ' + valueOrDash(item.equipmentCode) + '.');
    }

    function fillMovements(item) {
        $('#movements-equipment-subtitle').text(
            valueOrDash(item.equipmentCode) + ' · ' + valueOrDash(item.equipmentName)
        );
        const template = document.getElementById('movement-template-' + item.equipmentId);
        $('#movements-table-body').html(template ? template.innerHTML : '<tr><td colspan="5" class="px-4 py-10 text-center text-muted-foreground">No hay movimientos disponibles.</td></tr>');
    }

    function fillStatus(item, resetFields) {
        $('#status-equipment-id').val(item.equipmentId);
        $('#status-equipment-subtitle').text('Actualiza la disponibilidad de ' + valueOrDash(item.equipmentCode) + '.');
        $('#status-current-state').text(valueOrDash(item.equipmentStateLabel));
        if (resetFields) {
            $('#status-new-state').val('');
            $('#status-reason').val('');
        }
    }

    function fillDelete(item) {
        $('#delete-equipment-id').val(item.equipmentId);
        $('#delete-equipment-name').text('“' + valueOrDash(item.equipmentCode) + ' · ' + valueOrDash(item.equipmentName) + '”');
        $('#delete-equipment-confirmation').val('');
        updateDeleteState();
    }

    function handleAction(action, item) {
        if (action === 'detail') {
            fillDetails(item);
            showModal('#modal-equipment-detail');
        } else if (action === 'edit') {
            fillEdit(item);
            showModal('#modal-edit-equipment');
        } else if (action === 'movements') {
            fillMovements(item);
            showModal('#modal-equipment-movements');
        } else if (action === 'status') {
            fillStatus(item, true);
            showModal('#modal-equipment-status');
        } else if (action === 'delete') {
            fillDelete(item);
            showModal('#modal-delete-equipment');
            setTimeout(function () {
                $('#delete-equipment-confirmation').trigger('focus');
            }, 100);
        }
    }

    function validateForm(form) {
        const $form = $(form);
        $form.find('.equipment-form-control').removeClass('border-destructive ring-1 ring-destructive');
        if (form.checkValidity()) {
            const total = Number($form.find('[name="cantidadTotal"]').val());
            const available = Number($form.find('[name="cantidadDisponible"]').val());
            if (!Number.isNaN(total) && !Number.isNaN(available) && available > total) {
                const $available = $form.find('[name="cantidadDisponible"]');
                $available.addClass('border-destructive ring-1 ring-destructive').trigger('focus');
                if (window.Swal) {
                    Swal.fire({icon: 'warning', title: 'Stock incorrecto', text: 'La cantidad disponible no puede superar la cantidad total.', confirmButtonColor: '#45d4b4'});
                }
                return false;
            }
            return true;
        }
        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) {
            $(firstInvalid).addClass('border-destructive ring-1 ring-destructive').trigger('focus');
        }
        if (window.Swal) {
            Swal.fire({icon: 'warning', title: 'Revisa el formulario', text: 'Completa correctamente los campos obligatorios.', confirmButtonColor: '#45d4b4'});
        }
        return false;
    }

    function updateDeleteState() {
        const valid = $('#delete-equipment-confirmation').val().trim() === 'ELIMINAR';
        $('#delete-equipment-submit').prop('disabled', !valid);
        $('#delete-equipment-help')
            .toggleClass('text-emerald-500', valid)
            .toggleClass('text-muted-foreground', !valid)
            .text(valid ? 'Confirmación correcta. Ya puedes dar de baja el equipo.' : 'El botón se habilitará al escribir la palabra completa.');
    }

    $(function () {
        createIcons();
        $('.equipment-modal').attr('aria-hidden', 'true');

        $('#btn-new-equipment').on('click', function () {
            showModal('#modal-new-equipment');
        });

        $('#btn-toggle-inventory-filters').on('click', function () {
            $('#inventory-filter-options').toggleClass('hidden').toggleClass('flex');
        });

        $('#inventory-state-filter, #inventory-category-filter').on('change', function () {
            $('#inventory-filter-form').trigger('submit');
        });

        $('#inventory-search').on('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                $('#inventory-filter-form').trigger('submit');
            }, 450);
        });

        $('.equipment-record').on('click', function (event) {
            if ($(event.target).closest('.equipment-actions').length) {
                return;
            }
            handleAction('detail', equipmentFromRecord($(this)));
        });

        $('.js-equipment-action').on('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            handleAction($(this).data('equipmentAction'), equipmentFromRecord($(this).closest('.equipment-record')));
        });

        $('.js-equipment-close-modal').on('click', function () {
            closeModal($(this).closest('.equipment-modal'));
        });

        $('.equipment-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAllModals();
            }
        });

        $('#new-equipment-form, #edit-equipment-form, #status-equipment-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-equipment-confirmation').on('input', function () {
            this.value = this.value.toUpperCase();
            updateDeleteState();
        });

        $('#delete-equipment-form').on('submit', function (event) {
            if ($('#delete-equipment-confirmation').val().trim() !== 'ELIMINAR') {
                event.preventDefault();
                $('#delete-equipment-confirmation').trigger('focus');
            }
        });

        const $page = $('#page-content');
        const success = $page.attr('data-success-message');
        const error = $page.attr('data-error-message');
        if (success && window.Swal) {
            Swal.fire({icon: 'success', title: 'Operación completada', text: success, timer: 3200, showConfirmButton: false});
        }
        if (error && window.Swal) {
            Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error, confirmButtonColor: '#45d4b4'});
        }

        const openModal = $page.attr('data-open-modal');
        if (openModal === 'nuevo') {
            showModal('#modal-new-equipment');
        } else if (openModal === 'editar') {
            showModal('#modal-edit-equipment');
        } else if (openModal === 'estado') {
            const id = $('#status-equipment-id').val();
            const $record = $('.equipment-record').filter(function () {
                return String($(this).data('equipmentId')) === String(id);
            }).first();
            if ($record.length) {
                fillStatus(equipmentFromRecord($record), false);
            }
            showModal('#modal-equipment-status');
        }
    });
})(jQuery);
