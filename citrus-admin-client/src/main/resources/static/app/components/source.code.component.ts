import {Component,  Input, OnChanges, AfterViewInit} from '@angular/core';
import {TestDetail} from "../model/tests";
import {TestService} from "../service/test.service";
import {Alert} from "../model/alert";
import {AlertService} from "../service/alert.service";

declare var ace;

@Component({
    selector: "source-code",
    template: `<pre id="{{id}}" class="code-editor"></pre>
               <button class="btn btn-primary" (click)="saveSourceCode()">Save</button>
               <button class="btn btn-primary" (click)="resetSourceCode()">Reset</button>`
})
export class SourceCodeComponent implements OnChanges, AfterViewInit {
    @Input('editor-id') id: string;
    @Input() type = "xml";
    @Input() detail: TestDetail;

    constructor(private _testService: TestService,
                private _alertService: AlertService) {}

    sourceCode = 'Loading sources ...';
    editor: any;

    ngOnChanges() {
        this.getSourceCode();
    }

    ngAfterViewInit() {
        this.editor = ace.edit(this.id);
        this.editor.setTheme("ace/theme/chrome");
        this.editor.session.setMode("ace/mode/" + this.type);
    }

    getSourceCode() {
        this._testService.getSourceCode(this.getRelativePath())
            .subscribe(
                sourceCode => {
                    this.sourceCode = sourceCode;

                    if (this.editor) {
                        this.editor.setValue(this.sourceCode, 1);
                        this.editor.resize();
                    }
                },
                error => this.notifyError(<any>error));
    }


    saveSourceCode() {
        this._testService.updateSourceCode(this.getRelativePath(), this.editor.getValue())
            .subscribe(error => this.notifyError(<any>error));
    }

    resetSourceCode() {
        this.getSourceCode();
    }

    notifyError(error: any) {
        this._alertService.add(new Alert("danger", error.message, false));
    }

    private getRelativePath() {
        if (this.type == "java" && this.detail.relativePath.endsWith(".xml")) {
            //In test-detail.html, there are 2 tabs (xml and java) when detail.type=XML and they both use the same editor (with only one type: xml!).
            //In this case, when the Java tab is selected, the java file has to be shown
            return this.detail.relativePath.replace(".xml", ".java");
        } else {
            return this.detail.relativePath;
        }
    }
}