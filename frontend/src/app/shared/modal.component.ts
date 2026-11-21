import { AfterViewInit, Component, ElementRef, input, output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    <dialog #dialog aria-labelledby="dialog-title" (cancel)="$event.preventDefault(); closed.emit()" (click)="backdrop($event)">
      <div class="modal-head"><h2 id="dialog-title">{{ title() }}</h2><button class="icon-button" aria-label="Fechar janela" (click)="closed.emit()">×</button></div>
      <ng-content />
    </dialog>`
})
export class ModalComponent implements AfterViewInit {
  title = input.required<string>();
  closed = output<void>();
  @ViewChild('dialog', { static: true }) dialog!: ElementRef<HTMLDialogElement>;
  ngAfterViewInit() { this.dialog.nativeElement.showModal(); }
  backdrop(event: MouseEvent) {
    const rect = this.dialog.nativeElement.getBoundingClientRect();
    if (event.target === this.dialog.nativeElement && (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom)) this.closed.emit();
  }
}
