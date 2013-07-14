<!-- Create Schedule -->
<h3>Create Schedule</h3>
<form id="createScheduleForm">
    <table>
        <th>Milestone</th><th>Dates</th>
        <tr>
            <td class="formLabelTd">Accceptance</td>
            <td><input type="text" id="acceptanceDatePicker" class="input-medium datepicker" name="acceptanceStartDate"/></td>
        </tr>
        <tr>
            <td class="formLabelTd">Midterm</td>
            <td><input type="text" id="midtermDatePicker" class="input-medium datepicker" name="midtermStartDate"/></td>
        </tr>
        <tr>
            <td class="formLabelTd">Final</td>
            <td><input type="text" id="finalDatePicker" class="input-medium datepicker" name="finalStartDate"/></td>
        </tr>
        <tr class="submitBtnRow">
            <td></td>
            <td><input id="createScheduleSubmitBtn" type="submit" class="btn btn-primary" value="Create"/></td>
        </tr>
    </table>
</form>