Public Class Form2
    Private Sub Button1_Click(sender As Object, e As EventArgs)
        MsgBox("Button1")
    End Sub

    Private Sub Button2_Click(sender As Object, e As EventArgs)
        MsgBox("Button2")
    End Sub

    Private Sub Button3_Click(sender As Object, e As EventArgs)
        MsgBox("Button3")
    End Sub


    Private Sub CButton2_ClickButtonArea(Sender As Object, e As MouseEventArgs) Handles CButton2.ClickButtonArea
        MsgBox("button area clicked")
    End Sub

    Private Sub CButton2_Click(sender As Object, e As EventArgs) Handles CButton2.Click
        MsgBox("button clicked")
    End Sub

    Private Sub GroupBox1_Enter(sender As Object, e As EventArgs) Handles GroupBox1.Enter

    End Sub

    Private Sub GroupBox1_Validating(sender As Object, e As System.ComponentModel.CancelEventArgs) Handles GroupBox1.Validating
    End Sub

    Private Sub CButton2_PreviewKeyDown(sender As Object, e As PreviewKeyDownEventArgs) Handles CButton2.PreviewKeyDown
        If e.KeyCode = Keys.Space Then
            MsgBox("space is pressed")

        End If

    End Sub

    Private Sub CButton2_KeyUp(sender As Object, e As KeyEventArgs) Handles CButton2.KeyUp
        If e.KeyCode = Keys.Space Then
            MsgBox("space is keyup")
        End If
    End Sub

    Private Sub CButton2_KeyPress(sender As Object, e As KeyPressEventArgs) Handles CButton2.KeyPress
        If e.KeyChar = " " Then
            MsgBox("space is press")
            e.Handled = True
        End If
    End Sub

    Private Sub CButton2_KeyDown(sender As Object, e As KeyEventArgs) Handles CButton2.KeyDown
        If e.KeyCode = Keys.Space Then
            MsgBox("space is down")
            e.Handled = True
        End If
    End Sub
End Class