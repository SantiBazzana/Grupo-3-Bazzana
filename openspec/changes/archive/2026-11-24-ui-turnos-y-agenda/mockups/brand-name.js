/**
 * Nombre de marca de los mockups de C-10, en un único lugar.
 * La fundación aprobada (design-system/brand.js, revisión 1) expone paleta y tipografía,
 * pero no un nombre de marca: se mantiene este archivo local para no cambiar la revisión.
 * Para renombrar, editar solo NOMBRE_MARCA.
 */
export const NOMBRE_MARCA = 'Turnos Odontología';

document.querySelectorAll('[data-brand-name]').forEach(function (nodo) {
  nodo.textContent = NOMBRE_MARCA;
});
