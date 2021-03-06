import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pills',
    template:`
    <div [class.pull-right]="navigation && pullRight">
      <ul class="nav nav-pills" [class.pills-bar]="navigation" [class.nav-justified]="justified" [class.nav-stacked]="stacked">
        <li *ngFor="let pill of pills" [class.active]="pill.active">
          <a href="{{pill.id}}" (click)="select(pill, $event)"><i *ngIf="pill.icon" class="{{pill.icon}}"></i>&nbsp;{{pill.title}}</a>
        </li>
      </ul>
    </div>
    <div [class.clearfix]="navigation && pullRight"></div>
    <ng-content></ng-content>
  `
})
export class Pills {
    @Input() navigation: boolean;
    @Input() justified: boolean;
    @Input() stacked: boolean;
    @Input('pull-right') pullRight: boolean;

    @Output() selected = new EventEmitter(true);

    pills: Pill[];

    constructor() {
        this.pills = [];
        this.justified = false;
        this.stacked = false;
        this.navigation = false;
        this.pullRight = false;
    }

    select(pill: Pill, event) {
        this.pills.forEach((pill: Pill) => {
            pill.active = false;
        });
        pill.active = true;

        this.selected.emit(pill);

        event.stopPropagation();
        return false;
    }

    addPill(pill: Pill) {
        this.pills.push(pill);
    }
}

@Component({
    selector: 'pill',
    template: `
    <div id="{{id}}" class="tab-pane" [hidden]="!active">
      <ng-content></ng-content>
    </div>
  `
})
export class Pill {
    @Input('pill-id') id: string;
    @Input('pill-title') title: string;
    @Input('pill-icon') icon: string;
    @Input() active: boolean;

    constructor(pills: Pills){
        pills.addPill(this);
    }
}
