<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Me.ElaCustomTextBox1 = New ElaCustomTextBoxControl.ElaCustomTextBox()
        Me.ElaCustomTextBox2 = New ElaCustomTextBoxControl.ElaCustomTextBox()
        Me.ElaCustomTextBox3 = New ElaCustomTextBoxControl.ElaCustomTextBox()
        Me.SuspendLayout()
        '
        'ElaCustomTextBox1
        '
        Me.ElaCustomTextBox1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomTextBox1.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomTextBox1.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomTextBox1.BorderThickness = ElaCustomTextBoxControl.ElaCustomTextBox.BorderThicknessEnum.Thick
        Me.ElaCustomTextBox1.Location = New System.Drawing.Point(312, 69)
        Me.ElaCustomTextBox1.Name = "ElaCustomTextBox1"
        Me.ElaCustomTextBox1.Size = New System.Drawing.Size(398, 20)
        Me.ElaCustomTextBox1.TabIndex = 0
        '
        'ElaCustomTextBox2
        '
        Me.ElaCustomTextBox2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomTextBox2.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomTextBox2.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomTextBox2.BorderThickness = ElaCustomTextBoxControl.ElaCustomTextBox.BorderThicknessEnum.Thick
        Me.ElaCustomTextBox2.Location = New System.Drawing.Point(312, 106)
        Me.ElaCustomTextBox2.Name = "ElaCustomTextBox2"
        Me.ElaCustomTextBox2.Size = New System.Drawing.Size(398, 20)
        Me.ElaCustomTextBox2.TabIndex = 1
        '
        'ElaCustomTextBox3
        '
        Me.ElaCustomTextBox3.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomTextBox3.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomTextBox3.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomTextBox3.BorderThickness = ElaCustomTextBoxControl.ElaCustomTextBox.BorderThicknessEnum.Thick
        Me.ElaCustomTextBox3.Location = New System.Drawing.Point(312, 143)
        Me.ElaCustomTextBox3.Name = "ElaCustomTextBox3"
        Me.ElaCustomTextBox3.Size = New System.Drawing.Size(398, 20)
        Me.ElaCustomTextBox3.TabIndex = 2
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.Color.White
        Me.ClientSize = New System.Drawing.Size(1026, 563)
        Me.Controls.Add(Me.ElaCustomTextBox3)
        Me.Controls.Add(Me.ElaCustomTextBox2)
        Me.Controls.Add(Me.ElaCustomTextBox1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.WindowState = System.Windows.Forms.FormWindowState.Maximized
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub

    Friend WithEvents ElaCustomTextBox1 As ElaCustomTextBoxControl.ElaCustomTextBox
    Friend WithEvents ElaCustomTextBox2 As ElaCustomTextBoxControl.ElaCustomTextBox
    Friend WithEvents ElaCustomTextBox3 As ElaCustomTextBoxControl.ElaCustomTextBox
End Class
